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
 */

public class Evaluate 
{
	final static Boolean True = new Boolean(true);

	private static boolean evalBoolean(IGraph expr, IGraph context) throws Exception
	{
		Object o = eval(expr, context);
		return toBoolean(o);
	}
	
	/** This evaluator tries to support $$ */
	
	private static Object eval (IGraph expr, IGraph context) throws Exception
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
		
		if (s.equals(Types.VAR) || s.equals(Types.VARE)) 
		{
			expr = expr.get(0);
			s = expr.getName();
			/* One level of indirection, max */
			if (s.equals(Types.VAR) || s.equals(Types.VARE)) {
				expr = expr.get(0);
				o = eval(expr,context);
				/* o must be a String or IGraph */
if (o!=null) {
	//System.out.println("o is "+o.getClass().getName()+":\n"+o);
}
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
		
		if (s.equals(Types.EXPR) ||	s.equals(Types.GROUP)) {
			
			/* Continue below evaluating the subnodes */
			ignore = true;
		}
		else if (s.equals(Types.PATH)) {
			o = get(expr.get(0), context);				
			return toScalar(o);
		}
		else if (type == 'p') {
			o = get(expr, context);				
			return toScalar(o);
		}
		else if (type == 'o') {
			if (s.equals("!") || s.equals("~"))
				o = eval_unary(expr, context);
			else
				o = eval_binary(expr, context);
			return toScalar(o);
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
// System.out.println("eval nodes from:\n"+expr);		
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
		return toScalar (r);
	}
	
	private static Object eval2 (IGraph expr, IGraph context) throws Exception
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
		
		if (s.equals(Types.EXPR) ||  s.equals(Types.VAR) || 
			s.equals(Types.VARE) ||	s.equals(Types.GROUP)) {
			
			/* Continue below evaluating the subnodes */
			ignore = true;
		}
		else if (s.equals(Types.PATH)) {
			o = get(expr.get(0), context);				
			return toScalar(o);
		}
		else if (type == 'p') {
			o = get(expr, context);				
			return toScalar(o);
		}
		else if (type == 'o') {
			if (s.equals("!") || s.equals("~"))
				o = eval_unary(expr, context);
			else
				o = eval_binary(expr, context);
			return toScalar(o);
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
// System.out.println("eval nodes from:\n"+expr);		
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
		return toScalar (r);
	}
	
	/** This evaluator returns a Graph.
	 * 
	 * It is used (now) only in evaluating IFunctions in get() below.
	 */

	
	private static IGraph evalGraph (IGraph e, IGraph g) throws Exception
	{  		
		boolean ignore=false;
//System.out.println("evalGraph in\n"+e);	
		if (e == null)
			return null;
		
		while (transparent(e) && e.size()==1) 
			e = e.get(0);
		
	    if (e.getValue() != null)
	    	return e;
		
		String s = e.getName();
		char type = type(s);
//System.out.println("el: "+s);
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
//System.out.println("i:"+i+", "+e.get(i).getName());			
			IGraph n = evalGraph(e.get(i),g);
			if (n == null)
				continue;
//System.out.println("evalGraph n: \n"+n);			
			if (transparent(n))
				r.addNodes(n);
			else
				r.add(n);
		}
//System.out.println("evalGraph r: \n"+r);		

// System.out.println("ignore "+ignore+", "+e.getName());
		return r;
	}


	private static Object eval_unary(IGraph e, IGraph g) throws Exception 
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

	private static Object invert(IGraph e, IGraph g) throws Exception 
	{
		Object o = new Boolean(!evalBoolean(e, g));
		return o;
	}

	/**
	 * logically invert an integer.
	 * 
	 * If Boolean, return the same as invert().
	 */

	private static Object negate(IGraph e, IGraph g) throws Exception 
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

	private static Object eval_binary(IGraph e, IGraph g) throws Exception 
	{
		/* Check for correct number of operands
		 */

		if (e.size() != 2)
			throw new Exception("Binary expression with " + e.size()
					+ " operands");

		String s = e.getName();
		
//System.out.println("oper "+s);	
// System.out.println("get(0):\n"+e.get(0));
//System.out.println("get(1):\n"+e.get(1));

		Object o2 = eval(e.get(1), g);

// if (o2!=null)System.out.println("o2: "+o2.getClass().getName());	

		if (s.equals("="))
			return _assign(e.get(0), o2, g);
		
		Object o1 = eval(e.get(0), g);		
//System.out.println("o1: "+o1);

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
	
	private static boolean _or(Object a, Object b)
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
	
	private static boolean _and(Object a, Object b)
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
	
	private static Object toScalar(Object o)
	{
		if (o == null) return null;

	    if (! (o instanceof IGraph))	
	    	return o;
	    
	    IGraph g = (IGraph) o;
	    
	    while (transparent(g) && g.size()==1)
	    	g = g.get(0);
	    
	    if (g.size()>0)
	    	return o;
	    
	    if (g.getValue() != null)
	    	return g.getValue();
		
		String s = g.getName();
		
		/* A root node without subelements is nothing */
		if (Graph._NULL.equals(s)) return null;
		
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

	private static Object _sum(Object a, Object b, boolean op) throws Exception 
	{
//if (a!=null)System.out.println("a=("+a.getClass().getName()+")"+a);	
//if (b!=null)System.out.println("b=("+b.getClass().getName()+")"+b);	
		
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
			return ""+a+b;
		}
		return null;
	}

	private static Object _mul(Object a, Object b, boolean op) throws Exception 
	{   
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
				return op ? new Double(((Double) b).doubleValue()
						/ ((Long) a).longValue()) : new Double(((Double) b)
						.doubleValue()
						* ((Long) a).longValue());
			else if (b instanceof Long)
				return op ? new Long(((Long) b).longValue()
						/ ((Long) a).longValue()) : new Long(((Long) b)
						.longValue()
						* ((Long) a).longValue());
		}
		return null;
	}

	private static Object _assign(IGraph path, Object o2, IGraph g) throws Exception
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

	private static Object _equals(Object a, Object b, boolean polarity)
			throws Exception 
	{
		return new Boolean(compare(a, b) ? polarity : !polarity);
	}

	private static boolean compare(Object a, Object b) 
	{
		
//System.out.println("Expressions.compare ["+a+"]/["+b+"]");
		if (a == null && b == null)
			return true;
	
//System.out.println("Expressions.compare ["+a.getClass().getName()+"]/["+b.getClass().getName()+"]");
		
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
	
	private static boolean _greater(Object a, Object b) 
	{
		if (a == null || b == null)
			return false;
		
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

	private static String trim(String s) 
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
	
	private static IGraph get(IGraph path, IGraph g) throws Exception 
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
			
//System.out.println("get: path elem:"+e);
//System.out.println("in context:\n"+node);

			if (Types.INDEX.equals(e)) 
			{
				/* !i
				 *   0
				 * next
				 */
				
				Object o = eval(path.get(0), g);
//System.out.println("get[]:\n"+path+"\n--\n"+o);				
				if (! (o instanceof Long) )
					throw new Exception ("non numerical index "+e);
				long l = ((Long)o).longValue();
//System.out.println("get[]: index: "+l+", g:\n"+node);
				if (l >= node.size())
					return null;
				tmpNode = node.get((int)l);
				node = tmpNode;
				
				path = prev.get(ix++);		// XXX not normal
//System.out.println("get[]: path after:\n"+path+"\nresult node:\n"+node);	
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
//System.out.println("tmpNode:\n"+tmpNode);

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
					
					/* We make a copy since we are going to modify */
					IGraph cp = (IGraph) path.clone();

					_adjust(cp.get(0));
					IGraph args = evalGraph(cp.get(0),g);
//System.out.println("get: adjusted:\n"+cp);					
//System.out.println("get: adjusted args:\n"+args);	

					IGraph no = new Graph(path.getName());
					no.add(args);					
					value = ((IFunction) value).exec(no);  

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
// System.out.println("node where object is set:"+node.getName());
// System.out.println(" - object is:"+value);
					tmpNode = node;
					path = prev;
				}
			}

			i++;
			node = tmpNode;
			
// System.out.println("get: node:\n"+node);			
			prev = path;
			path = path.get(0);
		}
// System.out.println("get: before returning: i="+i+", node:\n"+node);
		
		if (i==0 || node == null) 
			return null;

		if (list && node.size()>0) {
		    IGraph r = new Graph();
		    r.addNodes(node);
		    return r;
		}
		
//System.out.println("last elem "+e+", node name: "+node.getName());
		
		/*
		 * This avoids returning what we already know (ex: $a -> a)
		 */
        if (node.size() == 0 && e.equals(node.getName()))
	        return null;

        return node;
	}
	
	private static void _adjust(IGraph g)
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
	
	private static void set(IGraph path, Object value, IGraph g) 
	{
		
// System.out.println("set:\n"+path+"value=\n"+value);

		IGraph node = g, tmp;
		boolean path_end=false;
		
		if (path == null)
			return;
		
		// reach last known element
		// Bypass possible initial !path type spec
		
		// XXX Is this allways correct?
		
		while (Types.EXPR.equals(path.getName()) || Types.PATH.equals(path.getName() ))
			path = path.get(0);
	
//System.out.println("set: path:\n"+path);
//System.out.println("set: value: "+value.getClass().getName());

		while (true)
		{
			tmp = node.getNode(path.getName());	

			if (tmp == null) 
				break;
			
			node = tmp;
			IGraph w = path.get(0);
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
		
// System.out.println("set: node name: "+node.getName());		
// System.out.println("value class: "+value.getClass().getName());		
		if (value instanceof IGraph) 
		{
			tmp = (IGraph) value;

//System.out.println("graph\n"+tmp);

			
			/*if (transparent(tmp))
			    node.addNodes(tmp);
			else*/ 
				node.add(tmp);

		} 
		else if (value instanceof String) {
		    node.add((String)value);
		}
		/*
		else if (value instanceof Long) {
		    node.add("" + value);
		}*/
		else if (value != null ){
			node = node.add(Types.OBJECT);
			node.setValue(value);
		}
	}
	
	/** @deprecated : use getFunction */
	
	private static Object getExtension(String name, IGraph args, IGraph context) 
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
								ex2.printStackTrace();
								return c.newInstance();
							}
							catch (Exception ex3)
							{
								ex3.printStackTrace();
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
	
	private static IFunction getFunction(String name, IGraph context) 
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
		return g.getName().equals(Graph._NULL);
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
// System.out.println("toStrings: "+s);
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
