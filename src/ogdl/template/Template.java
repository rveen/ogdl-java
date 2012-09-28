package ogdl.template;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;

import ogdl.*;
import ogdl.support.*;

/** Template to tree converter. */

public class Template extends ComplexPath
{	
	IGraph t;
	
	public Template(CharSequence s, IEventHandler ev) throws Exception
	{
		super(s,ev);
	}

	public Template(CharSequence s) throws Exception 
	{
		super(s); //XXX this invokes cpath() in the ComplexPath. This is wrong.
		
		/* template = (variable? text?)* */
		while (variable() || literal())
			;
		
		t = ((EventHandlerGraph) handler).get();
	
		astExpr(t);	

		//astTempl(t,0,null,null);
		
		IGraph dest = new Graph();
		astTpl(t,0,dest);
		t = dest;

	}
	
	boolean literal() throws Exception 
	{
		int c, i = 0;
	
		sb.setLength(0);
		while ((c = read()) != '$') {
			if (c == -1)
				break;
			sb.append((char) c);
			i++;
		}

		if (i > 0)
			handler.event(sb.toString());

		unread();

		return i>0?true:false;
	}
	
	/*
	 * variable := $ [$] [!] path 
	 * variable := $\ 
	 * variable := $[$][!]( ... ) |
	 * $[$][!]{ ... }
	 * 
	 * XXX should send PATH_ID event
	 */

	public boolean variable() throws Exception 
	{
		int c = read();

		if (c != '$') {
			unread();
			return false;
		}

		c = read();
		if (c == '\\') {
			handler.event("$\\");
			return true;
		}

		boolean indirect = false;
		boolean silent = false;

		if (c == '$') {
			indirect = true;
			c = read();
		}

		if (c == '!') {
			silent = true;
			c = read();
		}
		
		if (c == '{') {
			silent = true;
			unread();
		}
		
		if (silent)
			handler.event(Types.VARE);
		else
			handler.event(Types.VAR);

		if (indirect) {
			handler.inc();
			handler.event(Types.VAR);
	    }
		
		handler.inc();

		if (c == '{') {
			codeblock();
		}
		
		/* delimited path code */
		else if (c == '[') {
			space();
			handler.event(Types.PATH);
			handler.inc();
			cpath();
			handler.dec();
			space();
			c = read();
			if (c != ']')
				throw new SyntaxException("Missing ]");
		}

		/* group = expression */
		else if (c == '(') {
			unread();
			group();
		}

		/* simple variable, a path */
		else {
			unread();
			handler.event(Types.PATH);
			handler.inc();
			cpath();
			handler.dec();
		}

		handler.dec();
		
		if (indirect)
			handler.dec();

		return true;
	}

    public String printToString(IGraph g) throws Exception
    {    
    	StringWriter sout = new StringWriter();
        print(t, sout, g, true);
        return sout.toString();
    }
    
    public void print(IGraph g) throws Exception
    {    
         print(t, new PrintWriter(System.out), g, true);
    }
    
    public void print(Writer w, IGraph g) throws Exception
    {    
         print(t, w, g, true);
    }

    static void print (IGraph t, Writer w, IGraph g, boolean print) throws Exception
    {
    	boolean cond = false;
    	
    	if (t == null)
    		return;
    	
    	for (int i=0; i<t.size(); i++) 
    	{
    		IGraph node = t.get(i);
    		String s = node.getName();
    		
    		if (s.equals(Types.VAR) || s.equals(Types.VARE) || s.equals(Types.PATH) || s.equals(Types.EXPR)) 
    		{
    			Object o = Evaluate2.eval(node,g);

    			if (print && o!=null && !s.equals(Types.VARE)) 
    			{  
    			    w.write(""+o);  			        
    			    w.flush();
    			}
    		}
    		else if (s.equals(Types.FOR))
    		{
    			IGraph p1 = node.get(0);
    			IGraph p2 = node.get(1);
    		
    			Object o = Evaluate2.eval(p2,g);

    			/* XXX Graph is Iterable so this may be superfluous: */
    			
    			if (o instanceof IGraph) 
    			{
					IGraph list = (IGraph) o;					
					
					for (int j = 0; j < list.size(); j++) {
						Evaluate2.set(p1, list.get(j), g);							
						print(node.get(2), w, g, print);
					}
				}
    			else if (o instanceof Iterable) 
    			{
					Iterator it = ((Iterable) o).iterator();				
					while (it.hasNext()) {
						Evaluate2.set(p1, it.next(), g);					
						print(node.get(2), w, g, print);
					}
    			}
    			else if (o instanceof Iterator) 
    			{
					Iterator it = (Iterator) o;				
					while (it.hasNext()) {
						Evaluate2.set(p1, it.next(), g);					
						print(node.get(2), w, g, print);
					}
    			}
    			else if (o instanceof String) 
    			{
    				/* Check if it is a number */
					
					try {
						int j = Integer.parseInt((String)o);
						
						if (j>0) {
						    for (int k=0; k<j; k++) {
							    Evaluate2.set(p1, ""+k, g);					
							    print(node.get(2), w, g, print);
						    }
						}
					}
					catch (Exception ex) {}
    			}
    			else if (o instanceof Long) 
    			{
    				long j = (Long) o;
					if (j>0) {
						    for (int k=0; k<j; k++) {
							    Evaluate2.set(p1, ""+k, g);					
							    print(node.get(2), w, g, print);
						    }
					}
    			}
    		}
    		else if (s.equals(Types.IF)) 
    		{
    			cond = Evaluate2.evalBoolean(node.get(0),g);
   			
    			if (cond == true) 
    				print(node.get(1),w,g,print);
    			else
    				print(node.get(2),w,g,print);
    		}
    		else if (s.equals(Types.ELSE)) {
    			if (cond == false) {
    				print(node.get(0),w,g,print);
    			}
    		}
    		else if (s.equals("$\\") && print) {
    			w.write("$");
    			w.flush();
    		}
    		else if (print) { 
    			w.write(s);
    			w.flush();
    		}
    	}
    }
    
    public String eval(IGraph g) throws Exception
    {    
    	StringWriter w = new StringWriter();
        print(t, w, g, true);
        return w.toString();
    }
    
    public String toString()
    {
    	return t.toString();
    }
    
    public static boolean isIndex(IGraph node)
    {
    	IGraph prev=null;
    	
    	while (true) {
    		if (node.size()>1) return false;
    		if (node.size()==0) break;
    		prev = node;
    		node = node.get(0);
    	}
    	
    	if (prev!=null && prev.getName().equals("!i"))
    		return true;
    	
    	return false;
    }
    
    public static void astExpr(IGraph g) {
		for (int i = 0; i < g.size(); i++) {
			IGraph node = g.get(i);

			if (Types.EXPR.equals(node.getName())) {
				ast(node);
				clean(node);
			}
			else
				astExpr(node);
		}

	}



	/**
	 * Indent 'for' and 'if' directives
	 */

	static int astTempl(IGraph g, int index, IGraph gg, IGraph elsee) {
		int i = index;
		boolean toggle = true;

		for (; i < g.size(); i++) {
			IGraph node = g.get(i);
			if (node == null)
				break;
			
			String name = node.getName();
						
			if (Types.VAR.equals(name) || Types.VARE.equals(name)) {
				
				String s = null;
				
				if (node.size() == 0) 
					break;
				
				s = node.get(0).getName(0);

				if ("for".equals(s)) {
					node = g.set(i, node.get(0).get(0));
					node.setName(Types.FOR);

					IGraph text = new Graph("!text");

					node.add(text);

					if (gg != null) {
						gg.add(node);
						g.remove(i);
						i = astTempl(g, i, text, null);
					} else
						i = astTempl(g, i + 1, text, null);
				} else if ("if".equals(s)) {
					node = g.set(i, node.get(0).get(0));
					node.setName(Types.IF);

					IGraph tru = new Graph("!true");
					IGraph fal = new Graph("!false");

					node.add(tru);
					node.add(fal);

					if (gg != null) {
						gg.add(node);
						g.remove(i);
						i = astTempl(g, i, tru, fal);
					} else
						i = astTempl(g, i + 1, tru, fal);
				} else if ("end".equals(s)) {
					g.remove(i);
					i--;
					
					return i;
				} else if ("else".equals(s)) {
					toggle = false;
					g.remove(i);
					i--;
				} else if (gg != null) {
					if (toggle)
						gg.add(node);
					else
						elsee.add(node);
					g.remove(i);
					i--;
				}
			} else if (gg != null) {
				
				if (toggle)
					gg.add(node);
				else
					elsee.add(node);
				g.remove(i);
				i--;

			}
		}
		
		return i;
	}
	
	static int astTpl(IGraph g, int ix, IGraph dest) 
	{
		int i=ix;
		for (; i < g.size(); i++) 
		{
			IGraph node = g.get(i);
			
			String name = node.getName();
			
			if (Types.VAR.equals(name) || Types.VARE.equals(name)) {
				
				String s = null;
				
				if (node.size() == 0) 
					break;
				
				s = node.get(0).getName(0); // !v !p [?]
				
				if ("if".equals(s)) {
				    i = addIf(g,i,dest);
				}
				else if ("for".equals(s)) {
				    i = addFor(g,i,dest);
				}
				else
					dest.add(node);
			}
			else
				dest.add(node);
		}
		return i;
	}
	
	static int addIf(IGraph g, int i, IGraph dest) 
	{    
	    IGraph node = new Graph(Types.IF);
	    node.add(g.get(i).get(0).get(0).get(0));
	    
	    IGraph tru = new Graph("!true");
		IGraph fal = new Graph("!false");

		node.add(tru);
		node.add(fal);
		
		dest.add(node);
		dest = tru;
	    
		i++;
		
	    for (; i < g.size(); i++) 
		{
			IGraph node2 = g.get(i);
			
			String name = node2.getName();
			
			if (Types.VAR.equals(name) || Types.VARE.equals(name)) {
				
				String s = null;
				
				if (node2.size() == 0) 
					break;
				
				s = node2.get(0).getName(0); // !v !p [?]
				
				if ("if".equals(s)) {
				    i = addIf(g,i,dest);
				}
				else if ("for".equals(s)) {
				    i = addFor(g,i,dest);
				}
				else if ("else".equals(s)) {
					dest = fal;
				}
				else if ("end".equals(s)) {					
					return i;
				}
				else 
					dest.add(node2);
			}
			else
				dest.add(node2);
		}
	    
	    return i;
	}
	
	static int addFor(IGraph g, int i, IGraph dest) 
	{        
	    IGraph node = new Graph(Types.FOR);
	    node.addNodes(g.get(i).get(0).get(0));
	    
	    IGraph text = new Graph("!text");

		node.add(text);
  		
		dest.add(node);

		dest = text;
	    
		i++;
		
	    for (; i < g.size(); i++) 
		{
			IGraph node2 = g.get(i);
			
			String name = node2.getName();
			
			if (Types.VAR.equals(name) || Types.VARE.equals(name)) {
				
				String s = null;
				
				if (node2.size() == 0) 
					break;
				
				s = node2.get(0).getName(0); // !v !p [?]
				
				if ("if".equals(s)) {
				    i = addIf(g,i,dest);
				}
				else if ("for".equals(s)) {
				    i = addFor(g,i,dest);
				}
				else if ("end".equals(s)) {					
					return i;
				}
				else 
					dest.add(node2);
			}
			else
				dest.add(node2);
		}
	    
	    return i;
	}
}
