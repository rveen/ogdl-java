/* OGDL, Ordered Graph Data Language 
 * (c) R.Veen, 2002-2010.
 * License: zlib (see http://ogdl.org/license.htm)
 */

package ogdl;

import java.io.IOException;

import ogdl.support.*;

/** Simple path parser.
 * 
 * $Id$
 */

public class Path extends Parser implements IPath
{	
	int index=-1;
	EventHandlerList ev=null;
	
    public Path (CharSequence path) throws IOException, SyntaxException
    {
    	super(path,new EventHandlerList());
    	ev = (EventHandlerList) handler;
    	path();
    }
    
    public Path (CharSequence path, IEventHandler ev) throws IOException, SyntaxException
    {
    	super(path,ev);
    	path();
    }

    public void reset()
    {
    	index = 0;
    }
    
    public boolean next()
    {
    	if (ev.v == null) return false;
    	
    	if (index<ev.v.size()-1) {
    		index++;
    		return true;
    	}
    	return false;
    }
    
    public boolean previous()
    {
    	if (index>=0) {
    		index--;
    		return true;
    	}
    	return false;
    }
    
    public String getElement()
    {
    	return (String) ev.v.get(index);
    }
    
    public int size()
    {
    	if (ev.v == null) return 0;
    	else return ev.v.size();
    }
}
