/* OGDL, Ordered Graph Data Language 
 * (c) R.Veen, 2001-2010.
 * License: zlib (see http://ogdl.org/license.htm)
 */

package ogdl;

import java.util.List;

/** A named graph node, with a list of subnodes.
    
    The characteristics of this structure are:
    
    <ul>
    <li> It is a directed graph, with one or more source nodes and one or
      more sink nodes. Source nodes will informally called 'root' nodes, and are
      those nodes with indegree 0. Sink nodes will be informally called 'leaf'
      nodes, and are those with outdegree 0.
    <li>The object is basically a named vector with edges to nodes. These nodes are
     thus ordered. The only attribute that a node has is a name (a text string).  
    <li> The edges don't hold information; they are only pointers between nodes.
    <li> The object is in fact a root to a set of subnodes. To represent a graph
      with several roots (source nodes), the object's (node's) name is set to a 
      particular text string that means 'transparent'.
    <li> Leaf nodes (defined as nodes that do not point to other nodes) 'contain'
      arbitrary objects.
    </ul>
    
    First version: 2001 oct 24.
    
    $Id$
 */

public interface IGraph extends Iterable<IGraph>
{
	/** Return the object at the given path, traversing the 
	 * graph from this node on. 
	 * 
	 * The path string is parsed into elements (XXX spec)
	 */
	
    Object       get (String path) throws Exception;
    
    /** Return a subnode by index */
    
    IGraph       get (int index);
    
    /** Return the subnode vector as a list */
    
    List<IGraph> get ();
    
    /** Return a subnode with the given name, or null. 
     * 
     * This function only looks in the current node and doesn't parse the
     * name into a path.
     */
    
    IGraph       getNode (String name);
    
    String		 getString(String path) throws Exception;
    
    IGraph       add (String path, Object value) throws Exception;
    IGraph       add (String name);
    IGraph       add (IGraph g);
    
    /** @deprecated same as add(String) */
    
    IGraph       addNode (String name);
    
    /** Add the subnodes of g to the current node */
    
    void         addNodes(IGraph g);

    
    IGraph       set (String path, Object value) throws Exception;
    IGraph       set (int index, IGraph node);
    
    /** Set the given graph inside the current one, overwritting existing paths */
    
    IGraph       set (IGraph g);
    
    IGraph       setNode (IGraph g);
    
    IGraph       setNode (String name, Object value);
    
    /** Return the first node with the given name, or create it */ 
    
    IGraph       node (String name);
    
    /** Remove all subnodes. */
    
    IGraph       remove (); 
    
    /** Remove a subnode by index */
    
    void         remove (int index);
    
    /** Remove a subnode by name */
    
    void         removeNode (String name);
    
    /** Get the name of this node */
    String 		 getName  ();
    
    /** Get the name of the nth subnode */
    
    String 		 getName  (int index);
    
    /** Get the value (Object) associated with this node */
    
    Object 		 getValue ();   
    
    /** Set the name of this node */
    
    void   		 setName  (String name);
    
    /** Associate a value (Object) to this node */
    
    void   		 setValue (Object value);
    
    /** Limit the size of this graph (its number of subnodes) */
    
    void		 setSize (int n);

    /** Return the number of subnodes pointing out of this node */
    
    int 		 size();
    
    /** Compares two graphs for equality. 
	 * 
	 * Only takes into account the name attribute.
	 */
    
    boolean      equals(IGraph g);
   
    /** Makes a copy of this graph */
    
    Object 		clone ();
    
    /** Makes a copy of (only!) this node */
    
    IGraph 		copy ();
    
    void   		merge (IGraph g) throws Exception;
    
    /** Print out the root node, or not. 
     * 
     * In any case, if the root node is null, it will not
     * be printed. This function is only useful when you don't
     * want to print a root that isn't null.
     */
    
    String      toString (boolean root);
    
    /** Return the subnode vector as an array of objets.
     * 
     * (This function is used in Graph.sort)
     */
    
    Object[]    toArray();
    
    /* XXX Maybe needed after all: a path may need a current position in order to
     * pass it throught different object types.
     */
    
    //Object     get (IPath path)  throws Exception;
    //String	 getString(IPath path) throws Exception;
    //IGraph     add (IPath  path, Object value) throws Exception;
    //IGraph     set (IPath path,  Object value) throws Exception;
}
