package ogdl.template;

import java.lang.reflect.Constructor;

import ogdl.*;
import ogdl.support.*;

/**
 * Expression evaluator.
 * 
 * This class evaluates a ComplexPath within a Graph.
 * 
 * Supported types:
 * 
 * Boolean
 * Long
 * Double
 * String
 * Graph
 * 
 * $Id$
 * Initial date: 2002
 * 
 * TODO (XXX):
 * 
 * - Late evaluation of numbers (only in math context!), else when
 *   printing text with only numbers, leading zeros are eliminated.
 *   
 * - !obj needed ?
 * 
 */

public class Evaluate 
{
	final static Boolean True = new Boolean(true);

	public static boolean evalBoolean(IGraph expr, IGraph context) throws Exception
	{
		Object o = eval(expr, context);
		return toBoolean(o);
	}
	
	/** This evaluator tries to support $$ */
	
	public static Object eval (IGraph expr, IGraph context) throws Exception
	{  		
		/** this flag is set to true when the name of the current node is
		 * not a value but a directive (such as Types.EXPR, etc).
		 */
		
        boolean ignore=false;  
        
        /* remove an eventual solitary null node */
		while (expr.size()==1 && transparent(expr)) 
			expr = expr.get(0);
		
		/* if 'expr' has its value set, return that */
	    if (expr.getValue() != null)
	    	return expr.getValue();
		
		String s = expr.getName();
		char type = type(s);
		
		Object o=null;
		
		/* Variables: $path, $$path, $!path, $$!path */
		
		if (s.equals(Types.VAR) || s.equals(Types.VARE)) 
		{
			expr = expr.get(0);
			s = expr.getName();
			
			/* $$: One level of indirection, max.
			 * 
			 *  The result of the evaluation is used as expression
			 *  to a new evaluation.
			 */
			
			if (s.equals(Types.VAR) || s.equals(Types.VARE)) 
			{
				expr = expr.get(0);
				o = eval(expr,context);
				
				/* o must be a String or IGraph */

				if (o instanceof String) {
					return eval(new Graph((String)o), context);
				}
				else if (o instanceof IGraph) {
					return eval((IGraph)o, context);
				}
				else {
					return null;
				}
			}
			else
				return eval(expr,context);
		}

		if (s.equals(Types.EXPR) ||	s.equals(Types.GROUP)) 
		{	
			/* Continue below evaluating the subnodes */
			ignore = true;
		}
		else if (s.equals(Types.PATH)) {
			o = get(expr.get(0), context);				
			return toScalar_LateEval(o);
		}
		else if (type == 'p') {
			o = get(expr, context);				
			return toScalar_LateEval(o);
		}
		else if (type == 'o') {
			if (s.equals("!") || s.equals("~"))
				o = eval_unary(expr, context);
			else
				o = eval_binary(expr, context);
			return toScalar_LateEval(o);
		}
		
		/* scalar types */
		
		else if (expr.size()==0) 
		{
			if (type == 'i') 
			    return  new Long(s);
		    if (type == 'f')
			    return new Double(s);
		    if (s.equals("true"))
			    return new Boolean(true);
		    if (s.equals("false"))
			    return new Boolean(false);
		   return unquote(expr.getName());
		}
		
		/* Evaluate the subnodes */
		
		if (expr.size() == 0 && ignore)
			return null;
		
		IGraph r = ignore? new Graph():new Graph(expr.getName());
	
		for (int i=0; i<expr.size(); i++) 
		{
			IGraph node;
			Object o2 = eval(expr.get(i),context);
			
			if (! (o2 instanceof IGraph) ) {
				if (o2 instanceof String)
					node = new Graph((String)o2);
				else {
				    node = new Graph(Types.OBJECT);
				    node.setValue(o2);
				}
			}
			else
				node = (IGraph) o2;
			
			if (o2 != null) {
				if (transparent(node))
					r.addNodes(node);
				else
			        r.add(node);
			}
		}			
		return toScalar_LateEval (r);
	}

	
	/** This evaluator returns a Graph.
	 * 
	 * It is used (now) only in evaluating IFunctions in get() below.
	 */

	
	static IGraph evalGraph (IGraph e, IGraph g) throws Exception
	{  		
		boolean ignore=false;

		if (e == null)
			return null;
		
		while (transparent(e) && e.size()==1) 
			e = e.get(0);
		
	    if (e.getValue() != null)
	    	return e;
		
		String s = e.getName();
		char type = type(s);

		if (s.equals(Types.EXPR) ||  s.equals(Types.VAR) || 
			s.equals(Types.VARE) ||	s.equals(Types.GROUP)) {
			
			// Just evaluate the subnodes 
			ignore = true;
		}
		
		else if (s.equals(Types.PATH)) 
			return get(e.get(0), g);		
		else if (type == 'p') 
			return get(e, g);				
		else if (type == 'o') {
			Object b;
			if (s.equals("!") || s.equals("~"))
				b = eval_unary(e, g);
			else
				b = eval_binary(e, g);
			
			if (b == null)
				return null;
			
			if (b instanceof IGraph) 
				return (IGraph) b;
			
			if (b instanceof String)
				return new Graph((String)b);
			
			IGraph c = new Graph(Types.OBJECT);
			c.setValue(b);
			return c;
		}
		
		// scalar types 
		
		else if (e.size()==0) 
		   return new Graph(unquote(e.getName()));
		
		// Evaluate the subnodes 
		
		IGraph r = ignore? new Graph():new Graph(e.getName());
		
		if (e.size() == 0 && ignore)
			return null;
			
		for (int i=0; i<e.size(); i++) {
		
			IGraph n = evalGraph(e.get(i),g);

			if (n == null)
				continue;
			
			while (transparent(n) && n.size()==1) 	// XXX: Integrate into evalGraph
				n = n.get(0);
				
			if (n.size()==0 || Types.ARG.equals(n.getName()))
				r.add(n);
			else
			    r.add(Types.ARG).add(n);		
		}

		return r;
	}


	static Object eval_unary(IGraph e, IGraph g) throws Exception 
	{
		// Check for correct number of operands
		if (e.size() != 1)
			throw new Exception("Unary expression with " + e.size()
					+ " operands");

		String s = e.getName();
		if ("!".equals(s))
			return invert(e.get(0), g);
		else if ("~".equals(s))
			return negate(e.get(0), g);
		else
			throw new Exception("programmer error: unknown unary op: " + s);
	}

	static Object invert(IGraph e, IGraph g) throws Exception 
	{
		Object o = new Boolean(!evalBoolean(e, g));
		return o;
	}

	/**
	 * logically invert an integer.
	 * 
	 * If Boolean, return the same as invert().
	 */

	static Object negate(IGraph e, IGraph g) throws Exception 
	{
		Object o = eval(e, g);

		if (o instanceof Long) {
			long l = ((Long) o).longValue();
			return new Long(~l);
		}

		if (o instanceof Boolean) {
			boolean b = ((Boolean) o).booleanValue();
			return new Boolean(!b);
		}

		throw new Exception("Cannot negate (~) a " + o.getClass().getName());
	}

	static Object eval_binary(IGraph e, IGraph g) throws Exception 
	{
		// Check for correct number of operands

		if (e.size() != 2)
			throw new Exception("Binary expression with " + e.size()
					+ " operands");

		String s = e.getName();

		Object o2 = eval(e.get(1), g);

		if (s.equals("="))
			return _assign(e.get(0), o2, g);
		
		Object o1 = eval(e.get(0), g);		

		if (s.equals("+"))
			return _sum(o1,o2, false);
		else if (s.equals("-"))
			return _sum(o1,o2, true);
		else if (s.equals("*"))
			return _mul(o1,o2, false);
		else if (s.equals("/"))
			return _mul(o1,o2, true);
		else if (s.equals("=="))
			return _equals(o1,o2, true);
		else if (s.equals("~="))
			return _contains(o1,o2);
		else if (s.equals("!="))
			return _equals(o1,o2, false);
		else if (s.equals(">"))
			return new Boolean (_greater(o1,o2));
		else if (s.equals(">="))
			return new Boolean (_greater(o1,o2) || compare(o1,o2));
		else if (s.equals("<"))
			return new Boolean(_greater(o2,o1));
		else if (s.equals("<="))
			return new Boolean(_greater(o2,o1) || compare(o1,o2));
		else if (s.equals("||"))
			return new Boolean(_or(o1,o2));
		else if (s.equals("&&"))
			return new Boolean(_and(o1,o2));

		return null;
	}
	
	static boolean _or(Object a, Object b)
	{
		boolean x=false, y=false;
		
		if (a instanceof Boolean)
			x = ((Boolean)a).booleanValue();
		else if (a instanceof String)
			x = "true".equals(a)? true:false;
		
		if (b instanceof Boolean)
			y = ((Boolean)b).booleanValue();
		else if (b instanceof String)
			y = "true".equals(b)? true:false;
		
		return x || y;
	}
	
	static boolean _and(Object a, Object b)
	{
		boolean x=false, y=false;
		
		if (a instanceof Boolean)
			x = ((Boolean)a).booleanValue();
		else if (a instanceof String)
			x = "true".equals(a)? true:false;
		
		if (b instanceof Boolean)
			y = ((Boolean)b).booleanValue();
		else if (b instanceof String)
			y = "true".equals(b)? true:false;
		
		return x && y;
	}
	
	/**
	 * If the object is IGraph without
	 * subnodes, it looks at the name
	 * and returns:
	 * 
	 * getValue() if Types.OBJECT
	 * Long if integer number
	 * Double if floating point
	 * Boolean if 'true' or 'false'
	 * 
	 * Else it returns the same object.
	 */
	
	public static Object toScalar(Object o)
	{
		if (o == null) return null;

	    String s = null;
		
		if (o instanceof IGraph)
	    {

			IGraph g = (IGraph) o;

			while (transparent(g) && g.size() == 1)
				g = g.get(0);

			if (Types.OBJECT.equals(g.getName()))
				return g.getValue();

			if (g.size() > 0)
				return o;

			if (g.getValue() != null)
				return g.getValue();

			s = g.getName();

			/* A root node without subelements is nothing */
			if (Graph._NULL.equals(s))
				return null;
		} 
		else if (o instanceof String) 
			s = (String) o;
		else
			return o;
	        
		char type = type(s);
    
	    try {
			switch (type) {

			case 'f':
				return new Double(s);
			case 'i':
				return new Long(s);
			case 'b':
				return new Boolean(s);
			case 's':
				return trim(s);

			default:
				return s;
			}
		} catch (Exception ex) {
			return s;
		}
	}
	
	public static Object toScalar_LateEval(Object o)
	{
		if (o == null) return null;
	    
		if (! (o instanceof IGraph)) 		
	    	return o;
	    
	    IGraph g = (IGraph) o;
	    
	    while (transparent(g) && g.size()==1)
	    	g = g.get(0);
	    
	    if (Types.OBJECT.equals(g.getName()))
			return g.getValue();
	    
	    if (g.size()>0)
	    	return o;
	    
	    if (g.getValue() != null)
	    	return g.getValue();
		
		String s = g.getName();
		
		/* A root node without subelements is nothing */
		if (Graph._NULL.equals(s)) return null;
	
		/* Now we just return the name as a string */

		return trim(s);
	}

	static Object _sum(Object a, Object b, boolean op) throws Exception 
	{	
		if (a instanceof Integer) {
        	a = new Long((Integer) a);
        }
        if (b instanceof Integer) {
        	b = new Long((Integer) b);
        }
		
		if (a instanceof Double) {
			
			if (b instanceof String)
				b = toScalar((String)b);
			
			if (b instanceof Long)
				return op ? new Double(((Double) a).doubleValue()
						- ((Long) b).longValue()) : new Double(((Double) a)
						.doubleValue()
						+ ((Long) b).longValue());
			else if (b instanceof Double)
				return op ? new Double(((Double) a).doubleValue()
						- ((Double) b).doubleValue()) : new Double(((Double) a)
						.doubleValue()
						+ ((Double) b).doubleValue());
		} else if (a instanceof Long) {
			
			if (b instanceof String)
				b = toScalar((String)b);
			
			if (b instanceof Double)
				return op ? ( new Double( - ((Double) b).doubleValue() + (Long) a).longValue() )
						: new Double(((Double) b).doubleValue() + ((Long) a).longValue());
			else if (b instanceof Long)
				return op ? new Long(((Long) a).longValue()
						- ((Long) b).longValue()) : new Long(((Long) b)
						.longValue()
						+ ((Long) a).longValue());
		} else if (a instanceof String) {	
			if (a==null && b == null) return "";
			if (a==null) return ""+b;
			if (b==null) return a;
			if (op) {
				long l=0;
				if (b instanceof String) {
					l = Long.parseLong((String)b);
				}
				else if (b instanceof Long) {
					l = (Long) b;
				}
				return Long.parseLong((String)a) - l;
			}
			else
			    return ""+a+b;
		}
		return null;
	}

	static Object _mul(Object a, Object b, boolean op) throws Exception 
	{   	
		if (a instanceof String)
			a = toScalar((String)a);

		if (b instanceof String)
			b = toScalar((String)b);

        if (a instanceof Integer) {
        	a = new Long((Integer) a);
        }
        if (b instanceof Integer) {
        	b = new Long((Integer) b);
        }
		
		if (a instanceof Double) {
			if (b instanceof Long)
				return op ? new Double(((Double) a).doubleValue()
						/ ((Long) b).longValue()) : new Double(((Double) a)
						.doubleValue()
						* ((Long) b).longValue());
			else if (b instanceof Double)
				return op ? new Double(((Double) a).doubleValue()
						/ ((Double) b).doubleValue()) : new Double(((Double) a)
						.doubleValue()
						* ((Double) b).doubleValue());
		} else if (a instanceof Long) {
			if (b instanceof Double)
				return op ? 
						new Double ( ((Long) a).longValue() / ((Double) b).doubleValue() ): 
						new Double ( ((Long) a).longValue() / ((Double) b).doubleValue() );
			else if (b instanceof Long)
				return op ? 
						new Long(((Long) a).longValue() / ((Long) b).longValue()) : 
					    new Long(((Long) a).longValue()	* ((Long) b).longValue());
		}
		return null;
	}

	static Object _assign(IGraph path, Object o2, IGraph g) throws Exception
	{	
		set(path, o2,g);
		return o2;
	}
	
	/** Test if b appears in a. For that both 
	 * should be strings.
	 */
	
	private static Object _contains(Object a, Object b)
			throws Exception 
	{
		if (a==null || b==null)
			return new Boolean(false);
			
	    String sa = a.toString();
	    String sb = b.toString();
	    
		sa = sa.toLowerCase();
		sb = sb.toLowerCase();
		
		return new Boolean( sa.indexOf(sb) != -1 );
	}

	static Object _equals(Object a, Object b, boolean polarity)
			throws Exception 
	{
		return new Boolean(compare(a, b) ? polarity : !polarity);
	}

	static boolean compare(Object a, Object b) 
	{
		if (a == null && b == null)
			return true;
	
		if (a == null || b == null)
			return false;

		a = toScalar(a); // First argument determines type.
		
		try {

			if (a instanceof Boolean) {
				if (b instanceof Boolean) {
					if (((Boolean) a).booleanValue() == ((Boolean) b)
							.booleanValue())
						return true;
				} else if (b instanceof String) {
					if (((Boolean) a).booleanValue()
							&& ((String) b).equals("true"))
						return true;
				}
				return false;
			}

			if (b instanceof Boolean) {
				if (a instanceof String) {
					if (((Boolean) b).booleanValue()
							&& ((String) a).equals("true"))
						return true;
				}
				return false;
			}

			if (a instanceof Double) {
				if (b instanceof Number) {
					if (((Number) a).doubleValue() == ((Number) b)
							.doubleValue())
						return true;
					return false;
				}
				if (b instanceof String) {
					if (((Number) a).doubleValue() == Double
							.parseDouble((String) b))
						return true;
					return false;
				}
				return false;
			}

			if (b instanceof Double) {
				if (a instanceof Number) {
					if (((Number) a).doubleValue() == ((Number) b)
							.doubleValue())
						return true;
					return false;
				}
				if (a instanceof String) {
					if (((Number) b).doubleValue() == Double
							.parseDouble((String) a))
						return true;
					return false;
				}
				return false;
			}

			if (a instanceof Long) {
				if (b instanceof Number) {
					if (((Number) a).longValue() == ((Number) b).longValue())
						return true;
					return false;
				}
				if (b instanceof String) {
					if (((Number) a).longValue() == Long.parseLong((String) b))
						return true;
					return false;
				}
				return false;
			}

			if (b instanceof Long) {
				if (a instanceof String) {
					if (((Number) b).longValue() == Long.parseLong((String) a))
						return true;
					return false;
				}
			}

			if (a instanceof String) {
				if (((String) a).equals(b))
					return true;
				return false;
			}
		} catch (Exception ex) {
			return false;
		}

		return false;
	}
	
	static boolean _greater(Object a, Object b) 
	{
		if (a == null || b == null)
			return false;
		
		if (a == null || b == null)
			return false;

		a = toScalar(a); // First argument determines type.
		
		try {

			if (a instanceof Double) {
				if (b instanceof Number) {
					if (((Number) a).doubleValue() > ((Number) b)
							.doubleValue())
						return true;
					return false;
				}
				if (b instanceof String) {
					if (((Number) a).doubleValue() > Double
							.parseDouble((String) b))
						return true;
					return false;
				}
				return false;
			}

			if (b instanceof Double) {
				if (a instanceof Number) {
					if (((Number) a).doubleValue() > ((Number) b)
							.doubleValue())
						return true;
					return false;
				}
				if (a instanceof String) {
					if (((Number) b).doubleValue() < Double
							.parseDouble((String) a))
						return true;
					return false;
				}
				return false;
			}

			if (a instanceof Long) {
				if (b instanceof Number) {
					if (((Number) a).longValue() > ((Number) b).longValue())
						return true;
					return false;
				}
				if (b instanceof String) {
					if (((Number) a).longValue() > Long.parseLong((String) b))
						return true;
					return false;
				}
				return false;
			}

			if (b instanceof Long) {
				if (a instanceof String) {
					if (((Number) b).longValue() < Long.parseLong((String) a))
						return true;
					return false;
				}
			}

		} catch (Exception ex) {
			return false;
		}

		return false;
	}

	/** Strip white space and quotes at both ends on the given string. */

	public static String trim(String s) 
	{
		if (s == null)
			return null;
		s = s.trim();
		int len = s.length();

		if (len < 2)
			return s;
		if (s.charAt(0) == '"' && s.charAt(len - 1) == '"')
			return s.substring(1, len - 1);
		if (s.charAt(0) == '\'' && s.charAt(len - 1) == '\'')
			return s.substring(1, len - 1);
		return s;
	}
	
	/** creates a new Graph with the result of applying path to g.
	 */
	
	public static IGraph get(IGraph path, IGraph g) throws Exception 
	{
		IGraph node = g, tmpNode, prev=null;
		boolean list=true;
		int ix=1;

		if (path == null)
			throw new Exception("argument error: path is null");

		if (g == null)
			throw new Exception("argument error: context is null");

		int i=0;
		
		String e=null;
		
		while (path != null)
		{
			e = path.getName();

			if (Types.INDEX.equals(e)) 
			{
				/* !i
				 *   0
				 * next
				 */
				
				Object o = eval(path.get(0), g);
			
				if (! (o instanceof Long) )
					throw new Exception ("non numerical index "+e);
				long l = ((Long)o).longValue();

				if (l >= node.size())
					return null;
				tmpNode = node.get((int)l);
				node = tmpNode;
				
				path = prev.get(ix++);		// XXX not normal

				i++;
				// list = false;
				continue;
			}
			else if ("_this".equals(e)) {
			    tmpNode = node;
			    list = true;
			}
			else {
				tmpNode = node.getNode(e);
				list = true;
			}

			if (tmpNode==null && Types.OBJECT.equals(node.getName(0))) {
				node = node.get(0);
			}

			if (tmpNode == null) 
			{			
				Object value = node.getValue();

				if ("_name".equals(e)) {
					return new Graph( node.getName() );
				}
				else if ("_size".equals(e)) {
					return new Graph( "" + node.size() );
				}
				else if ("_value".equals(e)) {
					IGraph ret = new Graph(Types.OBJECT);
					ret.setValue(node.getValue());
					return ret;
				}
				else if (value instanceof IFunction) 
				{

					/* during evaluation, values are possibly
					 * converted to objects.
					 */

					/* XXXXXXXXXXX Need to be cleaned and make a consistent
					 * evaluation of a  complex path as in
					 * 
					 * $store.get(id)._doc.h1
					 */
					
					IGraph args = new Graph(path.getName());
					
					/* We make a copy since we are going to modify */
					
					IGraph cp = (IGraph) path.clone();

					/* XXX This part to be redesigned 
					 * 
					 * Now a(1,2) and a(1 2) evaluate to the same!
					 * 
					 */
					
					for (IGraph arg : cp) {
					    _adjust(arg);
					    IGraph a = evalGraph(arg,g);
					    args.add(a);
					}
					
					value = ((IFunction) value).exec(args);  

					if (value == null)
						return null;
					
					if (value instanceof IGraph)
						return (IGraph) value;
					
					else {
						IGraph ret = new Graph(Types.OBJECT);
						ret.setValue(value);
						return ret;
					}
				} 
										
				else {		
				
					// Check if extensible
					IGraph ex = node.getNode(Types.TYPE);
					
					if (ex == null)
						return null;

					value = getExtension(ex.getName(0), node, g);
					if (value == null)
						return null;

					node.setValue(value);

					tmpNode = node;
					path = prev;
				}
			}

			i++;
			node = tmpNode;
						
			prev = path;
			path = path.get(0);
		}
		
		if (i==0 || node == null) 
			return null;

		if (list && node.size()>0) {
		    IGraph r = new Graph();
		    r.addNodes(node);
		    return r;
		}
		
		/*
		 * This avoids returning what we already know (ex: $a -> a)
		 */
        if (node.size() == 0 && e.equals(node.getName()))
	        return null;

        return node;
	}
	
	static void _adjust(IGraph g)
	{
		if (g==null) return;
		
		if (g.getName().startsWith("!"))
			return;
		
		else {
			g.setName("\""+g.getName()+"\"");
			if (g.size()!=0)
				_adjust(g.get(0));
		}
	}
	
	public static void _set(IGraph path, Object value, IGraph g) throws Exception 
	{
		IGraph node = g, tmp;
		boolean path_end=false;
		
		if (path == null)
			return;
		
		// reach last known element
		// Bypass possible initial !path type spec
		
		// XXX Is this allways correct?
		
		while (Types.EXPR.equals(path.getName()) || Types.PATH.equals(path.getName() ))
			path = path.get(0);

		while (true)
		{
			String p = path.getName();
			
			int next = 0;
			long index = -1;
			
			if (p.equals(Types.INDEX)) {
				Object o = eval(path.get(0),g);
				try {
					index = ((Long) o);	
					next++;
				}
				catch (Exception ex)
				{
					return;
				}
			}
			
			if (index>-1) 
				tmp = node.get((int) index);
			else
			    tmp = node.getNode(path.getName());	

			if (tmp == null) 
				break;
			
			node = tmp;
			IGraph w = path.get(next);
			if (w == null) {
				path_end = true;
				break; // end of path 
			}
			path = w;
		}
		
		// create remaining nodes

		while (!path_end) 
		{
			String e = path.getName();
			if ("_name".equals(e)) {
				// XXX clean this up (but we need this func.				
				node.setName(value.toString());
			    return;
		    }
			node = node.add(e);
			path = path.get(0);
			if (path == null) break; // end of path
		}

		node.remove();  // remove subnodes
			
		if (value instanceof IGraph) 
		{
			tmp = (IGraph) value;
			node.add(tmp);

		} 
		else if (value instanceof String) {
		    node.add((String)value);
		}
		else if (value != null ){
			node = node.add(Types.OBJECT);
			node.setValue(value);
		}
	}
	
	static void set(IGraph path, Object value, IGraph g) throws Exception 
	{
		IGraph node = g, tmp, prev=null;
		
		if (path == null)
			return;
		
		while (Types.EXPR.equals(path.getName()) || Types.PATH.equals(path.getName() ))
			path = path.get(0);

		while (true)
		{
			String p = path.getName();
			
			if ("_name".equals(p)) {			// XXX hack needed for what?
				node.setName(value.toString());
			    return;
		    }
			
			int next = 0;
			long index = -1;
			
			if (p.equals(Types.INDEX)) {
				Object o = eval(path.get(0),g);
				o = toScalar(o);
		
				try {
					index = ((Long) o);

					next++;
				}
				catch (Exception ex)
				{
					return;
				}
			}	

			if (index>-1)
			{	
				tmp = node.get((int) index);	
				
				if (tmp == null) 
				{	
					while (node.size() <= index) {
						int i = node.size();
						IGraph ix = new Graph("__"+i);
						node.add(ix);
					}
					
					node = node.get((int)index);								
				}
				else
					node = tmp;
	
				path = prev;
			}
			else {
			    tmp = node.getNode(p);	
			    if (tmp == null)
			    	node = node.add(p);
			    else
			    	node = tmp;
			}
			
			prev = path;
			path = path.get(next);
			
			if (path == null) {
				break; 
			}
		}
		
		node.remove();  // remove subnodes
			
		if (value instanceof IGraph) 
		{
			tmp = (IGraph) value;
			node.add(tmp);				
		} 
		else if (value instanceof String) {
		    node.add((String)value);
		}
		else if (value != null ){
			node = node.add(Types.OBJECT);
			node.setValue(value);
		}
	}
	
	/** @deprecated : use getFunction */
	
	public static Object getExtension(String name, IGraph args, IGraph context) 
	{
		Class c = null;

		try {			
			c = Class.forName(name);
			if (c == null)
				return null;

			/* check if this class implements a known interface */

			Class itf[] = c.getInterfaces();

			for (int i = 0; i < itf.length; i++) {				
				if (itf[i] == IGraph.class || itf[i] == IFunction.class) 
				{
					try {
					    Constructor co = c.getConstructor(new Class[] {
							IGraph.class, IGraph.class });
					    return co.newInstance(new Object[] { args, context });
					}
					catch (Exception ex) {						
						try {
					        Constructor co = c.getConstructor(new Class[] {
								IGraph.class });
					        
						    return co.newInstance(new Object[] { args });
						}
						catch (Exception ex2) {
							try {
								//ex2.printStackTrace();
								return c.newInstance();
							}
							catch (Exception ex3)
							{
								//ex3.printStackTrace();
								return null;
							}
						}
					}
				}
			}

			/* Class hasn't a known interface. Get an introspector. */

			LocalFunction intro = new LocalFunction(name, args);
			return intro;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public static IFunction getFunction(String name, IGraph context) 
	{
		Class c = null;

		try {			
			c = Class.forName(name);
			if (c == null)
				return null;

			/* check if this class implements IFunction */

			Class itf[] = c.getInterfaces();

			for (int i = 0; i < itf.length; i++) 
			{
				if (itf[i] == IFunction.class) {

					try {
						Constructor co = c.getConstructor(new Class[] { IGraph.class });
						return (IFunction) co.newInstance(new Object[] { context });
					} catch (Exception ex2) {
						try {
							ex2.printStackTrace();
							return (IFunction) c.newInstance();
						} catch (Exception ex3) {
							ex3.printStackTrace();
							return null;
						}
					}
				}
			}

			/* Class hasn't a known interface. Get an introspector. */

			LocalFunction intro = new LocalFunction(name, null);
			return intro;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	private static boolean transparent(IGraph g)
	{
		if (Graph._NULL.equals(g.getName()))
			return true;
		
		if (Types.ARG.equals(g.getName()))
			return true;
		
		return false;
	}
	
	public static int toInteger(Object o)
	{
		if (o instanceof Integer)
			return ((Integer)o).intValue();
		
		if (o instanceof String)
			return Integer.parseInt((String)o);		
		
		return -1;
	}
	
	public static boolean toBoolean (Object o)
	{
		if (!(o instanceof Boolean))
			return false;
		if (((Boolean) o).booleanValue() == true)
			return true;
		return false;
	}
	
	private static IGraph clean(IGraph g)
	{
	    String s = g.getName();
	    
        if (s.equals(Types.EXPR) || s.equals(Types.VAR)
				|| s.equals(Types.VARE) || s.equals(Types.GROUP)
				|| s.equals(Types.PATH)) 
        	
        	g = g.get(0);

		IGraph g2 = g.copy();
		
		clean_(g,g2);
		
		return g2;
	}
	
	static IGraph cleanAll(IGraph g)
	{
        IGraph g2 = clean(g);
		toStrings(g2);
		
		return g2;
	}
	
	private static void clean_(IGraph g, IGraph g2)
	{
        IGraph c=null;

		for (int i = 0; i < g.size(); i++) 
		{
			IGraph node = g.get(i);			
			String s = node.getName();

            if (s.equals(Types.EXPR) || s.equals(Types.VAR)
					|| s.equals(Types.VARE) || s.equals(Types.GROUP)
					|| s.equals(Types.PATH)) 
            {
				clean_(node,c==null?g2:c);
			}
            else {
            	c = node.copy();
            	
            	if (Types.OBJECT.equals(c.getName()) && 
                    c.getValue() instanceof String ) {
            		c.setName((String)c.getValue());
            		c.setValue(null);
            	}
                g2.add(c);
                clean_(node,c);
            }           
		}		
	}
	

	static void toStrings(IGraph g)
	{
		for (int i=0; i<g.size(); i++)
			toStrings(g.get(i));

		String s = g.getName();

		if (s.equals(Types.OBJECT)) {
				g.setName(""+g.getValue());
				g.setValue(null);
	    }
		
		g.setName( unquote(g.getName()) );
	}
	
    static String unquote(String s)
    {
    	if (s.length()<2)
    		return s;
    	if ('\'' == s.charAt(0))
    		return s.substring(1,s.length()-1);
    	if ('"' == s.charAt(0))
    		return s.substring(1,s.length()-1);
    	return s;
    }
    
    static char type(String s) 
    {
		if (s==null || s.length()==0)
			return 0;
		
		char c = s.charAt(0);

		/* A quoted string */
		if (c == '\'' || c == '"')
			return 's';

		/* A number */
		if (Character.isDigit(c)) {
			if (s.indexOf('.') != -1)
				return 'f';
			else
				return 'i';
		}

		/* A type definition */
		if (s.length() > 1 && c == '!' && Character.isLetter(s.charAt(1)))
			return 'y';

		/* An operator */
		if (Characters.isOperator(c))
			return 'o';

		/* A boolean */
		if (s.equals("true") || s.equals("false"))
			return 'b';

		/* A literal */
		return 'p';
	}
}
