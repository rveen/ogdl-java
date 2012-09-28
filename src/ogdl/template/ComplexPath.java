/* OGDL, Ordered Graph Data Language 
 * (c) R.Veen, 2002-2008.
 * License: see http://ogdl.org
 */

package ogdl.template;

import java.io.IOException;

import ogdl.*;
import ogdl.support.*;

/** This class parses a string and produces a graph. The string should contain a path
 * expression as per the following rules:
 *
 * cpath     = element (dot element)*
 *             note: a dot is optional before and after cqualifier and group.
 * element   = word | quoted | cqualifier | group
 * cqualifier = ('[' | '{') expr (']' | '}')
 * group     = '(' expr ( (space|comma) expr)* ')'
 * expr      = [op1] (path|group|number|quoted) (op2 [op1] (path|group|number|quoted) )*
 * kword     = 'true'|'false'|'null' are readed as paths.
 */

public class ComplexPath extends Parser
{
	IGraph g;
	
	public ComplexPath(CharSequence s, IEventHandler ev)
	{
		super(s,ev);
	}
	
	public ComplexPath(CharSequence text) throws Exception 
	{
		super(text, new EventHandlerGraph());

		cpath();
		g = ((EventHandlerGraph) handler).get();
		
		ast(g);
		clean(g);
	}
	
	public ComplexPath(CharSequence text, boolean parse) throws Exception 
	{
		super(text, new EventHandlerGraph());
		g = ((EventHandlerGraph) handler).get();
		
		if (parse) {
		    cpath();

		    ast(g);
		    clean(g);
		}
	}
	
	/** element() returns 0 if there is no element,
	 * 1 when a dot is mandatory if more elements concatenated
	 * to the path (after a string), -1 if dot is not mandatory
	 * but still may be present.
	 */
	
	int element() throws Exception
	{
		if ( word()!=null || quoted(0)!=null ){		
			handler.event(sb.toString());
			return 1;
		}
		
		if (cqualifier('[') || cqualifier('{') || group() ) {		
			return -1;
		}	
		
	    return 0;	
	}
	
	boolean cpath() throws Exception
	{
		int l;
		boolean d;
// System.out.println("cpath() [1]");		
		l = handler.level();
		
		while (element() != 0) 
		{	
			handler.inc();
			int c = read();
			if (c!='.' && c!='[' && c!='{' && c!='(') {
				unread();
				break;
			}
			if (c!='.')
				unread();
		}
		
		handler.level(l);
		
		return true;
	}
	
	public boolean cqualifier(char type) throws Exception 
	{			
		int c = read();
		if (c != type) {
			unread();
			return false;
		}

		handler.event(type=='['?Types.INDEX:Types.SELECTOR);		
		handler.inc();
		space();
		expression(); 
		space();
		handler.dec();
		handler.dec();

		c = read();
		if (type == '[' && c != ']')
			throw new SyntaxException("missing ]");
		else if (type == '{' && c != '}')
			throw new SyntaxException("missing }");
		
		return true;
	}

	public boolean group() throws Exception 
	{
		int c = read();
	
		if (c != '(') {
			unread();
			return false;
		}

		int level = handler.level();

		while (true) 
		{	
			space();
			expression();	
			space();		
			if (!comma())
				handler.inc();

			c = read();
		
			if (c == ')')
				break;
			unread();
		}
		
		handler.level(level);

		if (c != ')')
			throw new SyntaxException("missing )");

		return true;
	}
	
	/*
	 * Code block still to be done. Define a syntax:
	 * 
	 * expr; expr; 
	 */
	
	public boolean codeblock() throws Exception 
	{
		int c = read();
	
		if (c != '{') {
			unread();
			return false;
		}

		int level = handler.level();

		while (true) 
		{	
			space();
			expression();	
			space();		
			if (!comma())
				handler.inc();

			c = read();
		
			if (c == '}')
				break;
			unread();
		}
		
		handler.level(level);

		if (c != '}')
			throw new SyntaxException("missing }");

		return true;
	}
	
	/*
	 * expression(precedence) = unary_expression ( op2 unary_expression )*
	 * 
	 * path = pelement(.pelement)* pelement = word? (args | index | selector)?
	 * args = '(' expression (','|' ' expression)* ')' index = '[' expression
	 * ']' selector = '{' expression '}'
	 * 
	 * unary_expression = [op1] ( '(' expr ')' | symbol ) symbol = path | string |
	 * number
	 */

	/**
	 * Binary expression ::= unary_expression ( op2 unary_expression )*
	 * @throws Exception 
	 */

	public boolean expression() throws Exception 
	{
		space();
// System.out.println("expression()");
		handler.event(Types.EXPR);
		handler.inc();

		while (true) {
			if (!unary_expression())
				break;

			space();

			String op = operator();
			if (op == null) {
				break;
			}
			handler.event(op);

		}

		handler.dec();

		return true;
	}

	/**
	 * unary_expression ::= operator1? ( ( '(' expression ')' ) | symbol )
	 */

	public boolean unary_expression() throws Exception 
	{
		space();

		int c = read();
// System.out.println("unary_() [1]");
		// Unary operators
		if (c == '!' || c == '~') {
			handler.event("" + (char) c);
			space();
		}
		else if (c == '(') {
			unread();
			group();
			return true;
		}
		else
     		unread();
// System.out.println("unary_() [2]");

		String s = quoted(0);
		if (s!=null)
			handler.event('"' + s + '"');
		else {
			s = number();
			if (s == null)
				return cpath();

			handler.event(s);
		}
		return true;
	}
	
    public String operator() throws IOException
    {
    	int c, i=0;
    	
    	sb.setLength(0);
    	
		while (Characters.isOperator(c = read())) {
			sb.append((char) c);
			i++;
		}

		unread();
		return i > 0 ? sb.toString():null;
    } 
    
    public String number() throws IOException
    {
    	int c, i=0;
    	boolean dot=false;
    	
    	sb.setLength(0);
    	
		while (Character.isDigit(c = read())) {
			sb.append((char) c);
		
			i++;
			if ((c=read()) == '.') {
				if (dot) 
					break;
				else {
				    dot = true;
				    sb.append('.');
				}
			}
			else unread();
		}

		unread();
		return i > 0 ? sb.toString():null;
    } 
    
	/*
	 * convert expressions to syntax tree
	 * 
	 * XXX process unary first !
	 */

	public static void ast(IGraph g) {
		int i, ipre = 0, pre, prepre;
		IGraph node;

		if (g.size() == 0)
			return; // fast exit

		/* first do lower levels */

		for (i = 0; i < g.size(); i++)
			ast(g.get(i));

		while (true) {
			prepre = 0;
			for (i = 0; i < g.size(); i++) {
				node = g.get(i);
				String name = node.getName();
				pre = _precedence(name);
				if (node.getNode("__dirty") != null)
					pre = 0;

				if (pre > prepre) {
					prepre = pre;
					ipre = i;
				}
			}

			if (prepre > 0) {
				node = g.get(ipre);
				node.addNode("__dirty");
				node.add(g.get(ipre - 1));
				node.add(g.get(ipre + 1));
				g.remove(ipre - 1); // XXX sometimes < 0!!
				g.remove(ipre);
			} else
				break;
		}
	}

	public static void clean(IGraph g) {
		for (int i = 0; i < g.size(); i++) {
			IGraph node = g.get(i);
			clean(node);
			node.removeNode("__dirty");
		}
	}

	static String[] binary_operators = { "=", ">>=", "<<=", ">>>=", "+=", "-=",
			"*=", "/=", "%=", "&=", "^=", "|=", "||", "&&", "|", "^", "&",
			"!=", "==", "~=", ">", ">=", "<", "<=", "<<", ">>", ">>>", "+",
			"-", "*", "/", "%", "!", "~" };

	static int[] precedence = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3, 4, 5,
			6, 7, 7, 7, 8, 8, 8, 8, 9, 9, 9, 10, 10, 11, 11, 11, 100, 100 };

	static String[] sql_operators = { "and", "or", "like" };

	static String[] unary_operators = { "!", "~" };

	static int _precedence(String token) {
		for (int i = 0; i < binary_operators.length; i++) {
			if (token.equals(binary_operators[i]))
				return precedence[i];
		}
		return 0;
	}

	static boolean _binary(String s) {
		int i = _precedence(s);
		if (i > 0 && i < 100)
			return true;
		return false;
	}

	static boolean _unary(String s) {
		for (int i = 0; i < unary_operators.length; i++) {
			if (s.equals(unary_operators[i]))
				return true;
		}
		return false;
	}
	
	public String toString()
	{
		return ""+g;	
	}
	
	public IGraph graph()
	{
		return g;
	}
}
