/* OGDL, Ordered Graph Data Language 
 * (c) R.Veen, 2002-2010.
 * License: see http://ogdl.org/ (similar to zlib)
 */

package ogdl.support;

import ogdl.*;

/** This class models a function that accepts a Graph and returns a Graph.
 * 
 * $Id$
 */

public interface IGFunction
{
    IGraph exec (IGraph path) throws Exception;
  
    void close();	
}
