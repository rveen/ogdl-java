/* OGDL, Ordered Graph Data Language 
 * (c) R.Veen, 2002-2008.
 * License: see http://ogdl.org/
 */

package ogdl.support;

import java.io.IOException;
import java.io.Reader;

import ogdl.SyntaxException;

public class Parser {
	
	public IEventHandler handler;
	
	/** Used to track the line number, for the exclusive use
	 *  of reporting syntax errors. 
	 */
	
	int line=0;
	
	/** This variable is false if reading from a CharSequence, true
	 * if reading from a Reader.
	 */
	
	boolean source=false;
	protected Reader inr = null;
	protected CharSequence ing = null;
	
	int ingx = 0, ingm, savedSpaces = 0;
	
	/** A line starts with some level. Each node increments the level by one.
	 */
	
	int level = 0, lindex=0, levels[] = new int[64];
	
	/** This array stores the indentation of all lines for a set of levels
	 * from 0 upwards. If we come back from a higher to a lower level, we then
	 * know what the indentation of the lower one was.
	 */
	
	int indentation[] = new int[64];
	
	/** an index into indentation[].
	 */
	
	int lineLevel=0;
	
	
	/** Here we save the characters read by many of the productions,
	 * until they are sent to the event handler.
	 */
	
	public StringBuilder sb = new StringBuilder();


	public Parser(CharSequence s, IEventHandler ev)
	{
		ing = s;
		ingm = s==null?0:s.length();
		ingx = 0;
		handler = ev;
	}
	
    public Parser (Reader r, IEventHandler event) 
    {
        this.inr = r;
        this.handler = event;
        source = true;
    }
    
	/** returns the number of consecutive spaces from the stream;
	 *  zero, if none. Mixing of tabs and spaces throws an exception. 
	 *  
	 *  It uses 'savedSpaces' if they were set by block().
	 */
	
	public int spaceUniform() throws IOException, SyntaxException 
	{
		int i = 0;

		if (savedSpaces != 0) {
			i = savedSpaces;
			savedSpaces = 0;
			return i;
		}

		int c = read(), d;
		if (c!=9 && c!=32) {
			unread();
			return 0;
		}
		i++;
			
		while ((d=read()) == c)
			i++;
		
		if ( (c==9 && d==32) || (c==32 && d==9) )
		    throw new SyntaxException("Tabs and spaces mixed");

		unread();
		return i;
	}

	/** returns the number of consecutive spaces from the stream;
	 *  zero, if none.
	 *  
	 *  It uses 'savedSpaces' if they were set by block().
	 */
	
	public int space() throws IOException 
	{
		int i = 0;

		if (savedSpaces != 0) {
			i = savedSpaces;
			savedSpaces = 0;
			return i;
		}

		while (Characters.isSpace(read()))
			i++;

		unread();
		return i;
	}

	public boolean eos() throws IOException 
	{
		int c = read();
		unread();

		return Characters.isEnd(c) ? true : false;
	}

	public String word() throws IOException {
		int i = 0, c;

		sb.setLength(0);

		while (Characters.isWord(c = read())) {
			sb.append((char) c);
			i++;
		}

		unread();
		return i > 0 ? sb.toString():null;
	}
	
	public String string() throws IOException {
		int i = 0, c;

		sb.setLength(0);

		while (Characters.isString(c=read())) {
			sb.append((char) c);
			i++;
		}

		unread();
		return i > 0 ? sb.toString():null;
	}
	
	
	public String text() throws IOException {
		int i = 0, c;

		sb.setLength(0);

		while (Characters.isText(c = read())) {
			sb.append((char) c);
			i++;
		}

		unread();
		return i > 0 ? sb.toString():null;
	}

	public boolean newline() throws IOException 
	{
		line++;

		int c = read();

		if (c == '\r') {
			c = read();
			if (c != '\n')
				unread();
			return true;
		} else if (c == '\n')
			return true;

		line--;
		unread();
		return false;
	}

	public String quoted(int indentation) throws IOException 
	{
		int q = read();
		int c, cc = 0;
		int skip = 0;

		if (q != '"' && q != '\'') {
			unread();
			return null;
		}

		sb.setLength(0);

		while (true) {
			c = read();
			if (c == -1 || (c == q && cc != '\\'))
				break;

			if (skip > 0) {
				if (Characters.isSpace((char) c)) {
					skip--;
					cc = c;
					continue;
				} else
					skip = 0;
			}

			if (cc == '\\' && (c == '\'' || c == '"') )
				sb.setLength(sb.length()-1);
			
			sb.append((char) c);
			cc = c;
			if (c == '\n')
				skip = indentation + 1;
		}
		return sb.toString();
	}

	public String comment() throws IOException 
	{
		int i = 0, c;

		sb.setLength(0);

		if ((c = read()) == '#') {
			sb.append((char) c);
			i++;
			while (!Characters.isBreakOrEnd(c = read())) {
				sb.append((char) c);
				i++;
			}
		}

		unread();
		return i > 0 ? sb.toString() : null;
	}

	public String block(int indentation) throws IOException 
	{
		int c = read();
		int m, i;
		int ind = -1;	// load with indentation of first block line.

		if (c != '\\') {
			unread();
			return null;
		}

		space();
		
		if (!newline())
			return null; // XXX loosing chars (the \ and spaces)

		sb.setLength(0);

		while (true) 
		{
			m = space();
			
			if (ind<0)
				ind = m;
			
			if (m<ind && m>indentation) 
				ind = m;
			
			if (m < ind) {
				if (newline())
					sb.append('\n');
				else {
					savedSpaces = m;
					break;
				}
			} 
			else {	
				for (i = ind; i < m; i++)
					sb.append(' ');
				while (true) {
					c = read();
					if (c == -1 || c == '\n') {
						sb.append('\n');
						break;
					}
					sb.append((char) c);
				}
				if (c == -1)
					break;
			}
		}
		
		// remove trailing spaces and newlines
		
		while (sb.length()>0) {
			int l = sb.length() -1;
			c = sb.charAt(l);
			if (Characters.isSpace(c) || Characters.isBreak(c)) {
				sb.setLength(l--);
				continue;
			}
			break;
		}

		return sb.toString();
	}

	public boolean comma() throws IOException 
	{
		int c = read();
		if (c == ',') 
			return true;

		unread();
		return false;
	}
	
    public String separator() throws IOException
    {
        sb.setLength(0);
        int c = read();

        if ( c == '(' || c == ')' || c == ',' ) {
            sb.append((char) c);
            return sb.toString();         
        }
        else
            unread();

        return null;
    }

	public boolean dot() throws IOException {
		int c = read();
		if (c == '.')
			return true;

		unread();
		return false;
	}
	
	public boolean character(int ch) throws IOException 
	{
		int c = read();
		if (c == ch)
			return true;

		unread();
		return false;
	}

	public boolean qualifier(char type) throws IOException, SyntaxException 
	{
		int c = read();
		if (c != type) {
			unread();
			return false;
		}

		handler.event(type=='['?Types.INDEX:Types.SELECTOR);
        handler.inc();
		
		space();
		word(); // XXX should be expr() for expressions
		space();

		handler.dec();
		
		c = read();
		if (type == '[' && c != ']')
			throw new SyntaxException("missing ]");
		else if (type == '{' && c != '}')
			throw new SyntaxException("missing }");
		
		return true;
	}
	
    public boolean group(int indentation) throws IOException, SyntaxException 
    {
    	int c = read();
		if (c != '(') {
			unread();
			return false;
		}
		
		int lev = level;
		
	    space();
	    
	    while ( true )
	    {
	    	String s = null;
	    	
	    	if ((s=string())!=null)
	    	{
	    		handler.event(s);
	    		handler.level(++lev);
	    		lev++;
	    	}
	    	else if (space()>0) {}
	    	else if ((s=quoted(indentation))!=null)
	    	{
	    		handler.event(s);
	    		handler.level(++lev);
	    		lev++;
	    	}
	    	else if (comma()) 
	    	{
	    		lev = level;
	    		handler.level(lev);
	    	}
	    	else if (!group(indentation))
	    		break;
	    }
	    
	    space();
	    c = read();
	    if (c!=')')
	    	throw new SyntaxException("missing ): "+(char)c);
		
		level = lev;	/* after a group, level is restored. */
		handler.level(level);
        return true;	
    }
   

	public int node(int indentation) throws IOException, SyntaxException
	{
		String s;
		
		if ( (s=block(indentation)) != null ) {
			handler.event(s);
			return -1;
		}
		
		if ( group(indentation))
			return 1;
		
		if (comment()!=null)
			return 0;
		
		if (comma()) {	
		    level--;
		    if (level<0) level=0;
		    handler.level(level);
			return 1;
		}
		
		if ( quoted(indentation)==null && string()==null)
			return 0;

		int len = sb.length();

		if (len != 0)
			handler.event(sb.toString());

		handler.level(++level);

		return 1;
	}

	/**
	 * line() : space? ( node ( space node )* )? space? comment? newline
	 * 
	 * returns: 0 : EOS 1 : more
	 */

	public boolean line() throws Exception 
	{
		int n = spaceUniform();  	/* 'n' is the indentation in spaces */  
		
		if (newline())
			return true;	/* empty line */
		if (eos())
			return false;   /* end of stream */	
		
		/* map n to indentation level */
		
		if (n==0) {
			level = 0;
			indentation[0] = 0;
			levels[0] = 0;
			lindex=0;
			lineLevel = 0;
		} 
		else if (n > indentation[lineLevel]) {
			indentation[++lineLevel] = n;
		} 
		else if (n < indentation[lineLevel]) {
			while (lineLevel != 0) {
				if (n >= indentation[lineLevel])
					break;
				lineLevel--;
			}
		}

		level = lineLevel;
		handler.level(level);

		while ((n = node(n)) > 0)
			space();

		if (n > 0) { /* after a block don't eat spaces */
			space();
			newline();
		}

		comment();
		
		return !eos();
	}

	/** Used in Path.java */
	
	public boolean path() throws IOException, SyntaxException 
	{
		boolean ret=false;
		
		while (true) 
		{
			if (word()!=null) {
				handler.event(sb.toString());
				ret = true;
			}
			else if (dot()) {
				ret = true;
			}
			else if (qualifier('[')) {
				handler.event(sb.toString());
				ret = true;
			}
			else if (qualifier('{')) {
				handler.event(sb.toString());
				ret = true;
			}
			else break;
		}
		
		return ret;
	}
	

	/** parse ::= line* */

	public void parse() throws Exception 
	{
		try {
			while (line())
				;
		} catch (SyntaxException e) {
			handler.error(e, e.line);
		}
	}

	/* read / unread isolation layer. */

	boolean unGetFlag = false;
	int unChar;

	public void unread() {
		unGetFlag = true;
	}
	
	/** 
	 * Character reader front-end.
	 * 
	 * [!] It suppresses ASCII CR forever and ever.
	 */

	public int read() throws IOException
	{
		if (unGetFlag) {
			unGetFlag = false;
		} 
		else 
		{
			if (!source) 
			{
				if (ingx >= ingm)
					unChar = -1;
				else {
					unChar = ing.charAt(ingx++);
					if (unChar == 13)
						unChar = ing.charAt(ingx++);
				}
			} else 
			{
				unChar = inr.read();
				if (unChar == 13)
					unChar = inr.read();
			}
		}

	    return unChar;
	}
}