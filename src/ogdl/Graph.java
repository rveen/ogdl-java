/* OGDL, Ordered Graph Data Language 
 * (c) R. Veen, 2001-2010.
 * License: zlib (see http://ogdl.org/license.htm)
 */

package ogdl;

import java.util.Vector;
import java.util.List;
import java.util.Iterator;

import ogdl.support.*;

/**
 * This is a data structure for holding directed graphs, where all nodes
 * are Strings, except leafs that can be any Object. The edges in the graph
 * don't hold any information or attributes. 
 * 
 * $Id$
 * 
 * Initial date: 2001 oct 3
 * 
 * XXX Some functions not protected against cycles.
 */

public class Graph implements IGraph 
{
	protected String name;

	// XXX: rationale for separating the value in two variables: speed.
	
	protected Object value = null;

	protected Vector<IGraph> v = null;

	/** A node with name _NULL is a place holder for
	 *  subnodes, i.e., a list. It should be treated as
	 *  as a transparent node.
	 */
	
	public static final String _NULL =  Types.NULL;
	
	/** A node with its name set to _OBJ has its value set (to something
	 * other that a String, obviously).
	 * 
	 * XXX: It's not clear if such a node should be a leaf, but probably.
	 */
	
	public static final String _OBJ = Types.OBJECT;
	
	public Graph() {
		/* Warning: _NULL cannot be a zero length string unless EventHandlerGraph.event() is
		 * modified. Events with null length are currently ignored, and also not written to
		 * OGDL binary (see OgdlBinarryEmitter).
		 */
		this.name = _NULL;
	}

	public Graph(String name) {
		this.name = name;
	}

	public Graph(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	/** This function is part of the Iterable */
	
	public Iterator<IGraph> iterator()
	{
		return new GraphIterator(this);
	}
	
	/** This function is part of the Iterable */
	
	class GraphIterator implements Iterator<IGraph>
	{
		IGraph g;
		int i=0;
		
		GraphIterator(IGraph g)
		{
			this.g = g;
		}
		
		public boolean hasNext()
		{
			if (i>=g.size())
				return false;
			return true;
		}
		
		public IGraph next()
		{
			return g.get(i++);
		}
		
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
	
	public String getName() {
		return name;
	}
	
	public List<IGraph> get() {
		return v;
	}
	
	public Object getValue() {
		return value;
	}
	
	/** Return the name of a subnode */
	
	public String getName(int index) 
	{
		if (size()>index)
			return get(index).getName();
		else
		    return null;
	}

	public void setName(String name) 
	{
		if (name==null)
			this.name = _NULL;
		else
		    this.name = name;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	
	public void setSize (int n)
	{
		if (size()>n)
		    v.setSize(n);
	}
	
	public boolean isNull() {
		return this.name.equals(_NULL);
	}
	
	public boolean isNil() {
		return this.name.equals(_NULL);
	}

	public IGraph add(IGraph g) 
	{
		if (g == null)
			return null;

		if (v == null) // don't create the Vector before we need it
			v = new Vector<IGraph>();
		
		if (_NULL.equals(g.getName()))
			for (int i=0; i<g.size(); i++) 
				v.add(g.get(i));
	
		else
		    v.add(g);

		return g;
	}

	public IGraph add(String s) 
	{
		if (s == null || s.length() == 0)
			return null;

		return add(new Graph(s));
	}
	
	public IGraph addNode(String s) 
	{
		if (v==null)
			v = new Vector<IGraph>();
		
		IGraph node = new Graph(s);
		v.add(node);
		return node;
	}

	/** add node 'path' and give it value, such that value=get(path) */

	public IGraph add(IPath path, Object value, String type) 
	{
		IGraph node = this, tmpNode;

		// reach last known element

		while (path.next()) {
			String e = path.getElement();

			tmpNode = node.getNode(e);

			if (tmpNode == null) {
				path.previous();
				break;
			}
			node = tmpNode;
		}

		// create remaining nodes

		while (path.next()) {
			String e = path.getElement();

			Graph g = new Graph(e);

			node.add(g);
			node = g;
		}

		/* If 'value' is an IGraph and the root
		 * element is _ROOT, then add its elements
		 * (skipping the _ROOT), else add the complete IGraph.
		 */
		
		if (value instanceof IGraph) 
		{
			tmpNode = (IGraph) value;
			if (! _NULL.equals(tmpNode.getName()))
				node.add(tmpNode);
			else 
			    for (int i = 0; i < tmpNode.size(); i++) {
				    node.add(tmpNode.get(i));
			    }
		} 
		else if (value instanceof String)
			node.add((String)value);
		else 
			node.add(_OBJ).setValue(value);
		
		return node;
	}

	public IGraph add(IPath path, Object value) {
		return add(path, value, null);
	}

	public IGraph add(String path, Object value) throws Exception {
		return add((IPath) new Path(path), value, null);
	}

	/** put a subnode in place of the first subnode with the same name */

	public IGraph setNode(IGraph g) 
	{
		String name = g.getName();

		if (name == null)
			throw new IllegalArgumentException(
					"Trying to set and unnamed graph");

		int i;

		for (i = 0; i < size(); i++) {
			Graph node = (Graph) v.get(i);

			if (name.equals(node.getName()))
				break;
		}
		if (i == size())
			add(g);
		else {
			v.set(i, g);
		}
		
		return g;
	}

	/** set the given graph inside the current one, overwritting existing paths */
	
	public IGraph set (IGraph g) 
	{
		if (g == null)
			return this;
		
	    for (int i=0; i<g.size(); i++) {
	    	IGraph node = g.get(i);
	    	String name = node.getName();
	    	
	    	IGraph gg = getNode(name);
	    	if (gg==null)
	    		add(node);
	    	else {
	    		gg.remove();
	    	    gg.set(node);
	    	}
	    }
	    return this;
	}
	
	
	/** @deprecated 
	 * Redesign or delete this function: should use getNode. 
	 */
	
	public IGraph setNode(String name, Object value) 
	{
		return setNode(new Graph(name, value));
	}

	/** set the value giving a path */

	public IGraph set(IPath path, Object value) 
	{
		if (value == null) return this;
		
		IGraph node = this, tmp;

		// reach last known element

		while (path.next()) {
			String e = path.getElement();

			tmp = node.getNode(e);

			if (tmp == null) {
				path.previous();
				break;
			}
			node = tmp;
		}

		// create remaining nodes

		while (path.next()) {
			node = node.add(path.getElement());

		}
		node.remove();

		if (value instanceof IGraph) {
			node.addNodes((IGraph) value);
			
		} else if (value instanceof CharSequence)
		{
			node.add(((CharSequence)value).toString());
		}
		else {
			node = node.add(_OBJ);
			node.setValue(value);
		}
			
		return node;
	}

	/** set the value giving a path */

	public IGraph set(String path, Object value) throws Exception {
		return set((IPath) new Path(path), value);
	}

	/** return the number of subnodes */

	public int size() 
	{
		return (v==null)? 0 : v.size();
	}

	public Object get(IPath path) throws Exception 
	{
		IGraph node = this, tmpNode=null;
		boolean wasIndex = false;

		while (path.next()) 
		{
			String e = path.getElement();

			if (e.equals("!i"))
			{
				path.next();
				String n = path.getElement();
				int i = Integer.parseInt(n);

				if (i<0 || i >= node.size())
					return null;
				
				tmpNode = node.get(i);
				wasIndex = true;
			}
			else {
			    tmpNode = node.getNode(e);
			    wasIndex = false;
			}

			if (tmpNode == null) 
			{
				if ("_name".equals(e))
					return node.getName();
				else if ("_value".equals(e))
					return node.getValue();
				else
					return null;
			}
			node = tmpNode;
		}
		
		/* avoid returning what we already know: that is:
		 * a String equal to the last element of the path.
		 */
		
		if (!wasIndex && node!=null && node.size()<2) 
		{
			if (node.size() == 0)
				return null;
			
			if (node.get(0).size() == 0) {
				node = node.get(0);
				if (node.getName().equals(_OBJ))
					return node.getValue();
				else return node.getName();
			}
		}
		
		return node; 
	}

	public Object get(String path) throws Exception {
		return get((IPath) new Path(path));
	}
	
	public String getString(String path) throws Exception 
	{
		return getString((IPath) new Path(path));
	}
	
	public String getString(IPath path) throws Exception 
	{
		Object o = get(path);		
		if (o == null) return null;
		
		return o.toString();
	}
	
	public int getInt(String path) throws Exception 
	{
		Object o = get((IPath) new Path(path));		
		if (o == null) throw new Exception("not found");

        String s = o.toString();
        return Integer.parseInt(s);
	}

	/** get the subnode at index */

	public IGraph get(int index) {
		if (index < 0 || v == null || index >= v.size())
			return null;
		return (IGraph) v.get(index);
	}
	
	/** set the subnode at index */
	
	public IGraph set(int index, IGraph node) {
		if (index < 0 || v == null || index >= v.size())
			return null;
		
		v.set(index,node);
		return node;
	}
	
	/** return the first subnode with this name */

	public IGraph getNode(String name) 
	{
		if (name == null)
			return null;

		for (int i = 0; i < size(); i++) {
			Graph node = (Graph) v.get(i);
			if (name.equals(node.name))
				return node;
		}
		return null;
	}
	
	/** return the first subnode with this name, or create it */
	
	public IGraph node (String name)
	{
		IGraph node = getNode(name);
		if (node == null)
			node = addNode(name);
		return node;
	}

	/**
	 * Nodes in graph g are merged into this graph.
	 * 
	 * Nodes in this graph with the same name as nodes in g are deleted first.
	 */

	public void merge(IGraph g) throws Exception 
	{
		if (g == null)
			return;
			
		for (int i = 0; i < g.size(); i++) {
			// Check that this node is present here, else create it
			String name = g.getName(i);
			
			IGraph node = getNode(name);
			if (node == null)
				node = add(g.get(i));
			node.merge(g.get(i));
		}
	}

	/** remove all subnodes with the given name */

	public void removeNode(String name) {
		int len = size();

		for (int i = 0; i < len; i++) {
			Graph g = (Graph) v.get(i);

			if (name.equals(g.name)) {
				v.remove(i); // esto funciona ??
				i--;
				len--;
			}
		}
	}

	/** remove the given subnode */

	public void remove(int index) 
	{
		if (index >=0 && index < size())
			v.remove(index);
	}

	/** remove all subnodes with the given name and value */

	public void removeNode(String name, Object value) {
		int len = size();

		for (int i = 0; i < len; i++) {
			Graph g = (Graph) v.get(i);

			if (name.equals(g.name) && value.equals(g.value)) {
				v.remove(i); 
				i--;
				len--;
			}
		}
	}

	/** Remove all subnodes */

	public IGraph remove() 
	{
		if (v != null)
			v.clear();
		return this;
	}

	public String toString()
	{
		return toString(true);	
	}
	
    public String toString(boolean root)
	{
		String s = OgdlEmitter.toString(this, root);	
		
		if (s == null)
			return "";
		
		if (s.length() == 0)
			return "";

		if (s.charAt(0) == '"')
			return s.substring(1, s.length() - 2);
		
		if (s.charAt(s.length()-1) == '\n')
			return s.substring(0,s.length() - 1);
		
		return s;
	}

	public Object clone() 
	{
		Graph g = new Graph(this.name, this.value);
		try {
			for (int i = 0; i < this.size(); i++)
				g.add((IGraph) this.get(i).clone());
		} catch (Exception ex) {
		}

		return g;
	}
	
	public IGraph copy() 
	{
		return new Graph(this.name, this.value);
	}

	public void addNodes(IGraph g) 
	{
		if (g == null) return;
		
		for (int i = 0; i < g.size(); i++)
			add(g.get(i));
	}
	
	/** compares two graphs for equality. 
	 * 
	 * Only takes into account the name attribute.
	 * 
	 * XXX Not protected against cycles.
	 */
	
	
	public boolean equals(IGraph g)
	{
		/* First compare 'root' names */
		if (name == null) return false;
		if (!name.equals(g.getName())) return false;
		
		/* second, compare sizes */
		if (size() != g.size()) return false;
		
		/* now compare sub-nodes */
		for (int i=0; i<size(); i++)
		{
			if (!get(i).equals(g.get(i)))
				return false;
		}

		return true;
	}
	
	/** Return as array (List interface)
	 * 
	 * Used in Graphs.sort
	 */
	 
	public Object[] toArray()
	{
		return v==null?null:v.toArray();
	}
}
