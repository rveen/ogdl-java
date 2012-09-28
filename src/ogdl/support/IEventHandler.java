/* OGDL, Ordered Graph Data Language 
 * (c) R.Veen, 2002-2010.
 * License: see http://ogdl.org/ (similar to zlib)
 */

package ogdl.support;

/** Generic parser event API.

    Fixed structure for fast callbacks.
    
    $Id: IEventHandler.java 31 2010-07-30 09:20:42Z rolf $
    
    Initial date: March 2002.
 */

public interface IEventHandler
{      
	int  inc ();
	int  dec ();
	int  level ();
	void level (int lev);
    void event (String text);               
    void event (byte[] data);
    void error (Exception e, int line);
}
