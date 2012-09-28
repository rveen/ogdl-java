/* OGDL, Ordered Graph Data Language 
 * (c) R.Veen, 2002-2006.
 * License: see http://ogdl.org/ (similar to zlib)
 */

package ogdl;

import java.io.Reader;
import ogdl.support.*;

/** Parser for the OGDL text format.
*/

public final class OgdlParser extends Parser
{
	public OgdlParser (Reader r, IEventHandler event) 
    {
        super(r,event);
    }
}


