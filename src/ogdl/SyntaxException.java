/* OGDL, Ordered Graph Data Language 
 * (c) R.Veen, 2002-2010.
 * License: zlib (see http://ogdl.org/license.htm)
 * 
 * $Id$
 */

package ogdl;

public class SyntaxException extends Exception 
{
	private static final long serialVersionUID = 1L;

	public int line;

	public SyntaxException() {
		super();
	}

	public SyntaxException(String s) {
		super(s);
	}

	public SyntaxException(String s, int line) {
		super(s);
		this.line = line;
	}
}
