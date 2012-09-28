/* OGDL, Ordered Graph Data Language 
 * (c) R.Veen, 2002-2010.
 * License: see http://ogdl.org/ 
 */

package ogdl.support;

/** OGDL text character classes.

    It classifies integers into End, Break, Special and Text.
    Characters are represented by integers, so that -1 and
    any Unicode code point can be classified.

    $Id$
    
    Initial date: Jul 2008
*/

public final class Characters
{
	/** Return true for any character below 0x20 (space) which is 
	 * not 0x09 (tab) or 0x10 (newline) or 0x13 (return).
	 */
	
    public static boolean isEnd(int c)
    {
    	if (c<32 && c!=9 && c!=10 && c!=13)
    		return true;
    	return false;
    }
    
    /** Return true for any character below 0x20 (space) which is not 0x09. */
    
    public static boolean isBreakOrEnd (int c)
    {
        if (c<32 && c!=9) return true;
        return false;
    }
    
    /** space :: = TAB | SP */

    public static boolean isSpace(int c)
    {
        if (c == 9 || c == 32) return true;
        return false;
    }

    /** break ::= LF | CR */

    public static boolean isBreak(int c)
    {
        if (c == 10 || c == 13) return true;
        return false;
    }

    public static boolean isText(int c)
    {
    	if ( isEnd(c) || isBreak(c) || isSpecial(c) )
    		return false;
    	return true;
    }
    
    /** Word = number, letter, _, or any char > 127
     * 
     */
    
    public static boolean isWord(int c)
    {
    	if (c > 127)
    		return true;
    	
    	if (c>47 && c<58) return true;
    	if (c>64 && c<91) return true;
    	if (c>96 && c<123) return true;
    	if (c == '_') return true;
    	return false;
    }
    
    /** String (unquoted string)
     * 
     *  Any printable character not including: '(', ')', quotes or or ','
     */
    
    public static boolean isString(int c)
    {
    	if (c > 127)
    		return true;
    	   	
    	if (c<33 || c=='"' || c=='\'' || c=='(' || c==')' || c==',' || c==127)
    		return false;

    	return true;
    }


    

    public static boolean isSpecial(int c)
    {
        if (c == '(' || c == ')' || c == ',' || c == '#' || c == '"' || c=='\'') 
        	return true;
        return false;
    }
    
    public static boolean isPathSpecial(int c)
    {
        if (c == '[' || c == ']' || c == '{' || c == '}' || c == '.') 
        	return true;
        return false;
    }
    
    final static String s = "!%^&*-=+<>/?~|";
    
    public static boolean isOperator(int c)
    {
        return ( s.indexOf(c) == -1 ) ? false: true;
    }
    
}
