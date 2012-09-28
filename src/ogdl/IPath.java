/* OGDL, Ordered Graph Data Language 
 * (c) R.Veen, 2002-2010.
 * License: zlib (see http://ogdl.org/license.htm)
 */

package ogdl;

/**  Path tokenizer.
 *   
 *   A path (in OGDL) is an OGDL stream written in one line. See the OGDL
 *   path specification for details.
 *   
 *   $Id: IPath.java 31 2010-07-30 09:20:42Z rolf $
 *   
 *   Initial date: 2002 ago 21
 *   
 *   XXX Maybe this object needs a currentPosition() method.
 */

public interface IPath
{
    boolean next ();
    boolean previous();
    void    reset ();
    int     size ();
    
    String  getElement ();
}
