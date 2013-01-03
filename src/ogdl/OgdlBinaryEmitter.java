/* OGDL, Ordered Graph Data Language 
 * (c) R.Veen, 2002-2009.
 * License: zlib (see http://ogdl.org/license.htm)
 */

package ogdl;

import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class OgdlBinaryEmitter
{
    /** Binary OGDL emitter.
     * 
     *  Buffered by a byte array. If the elements of the
     *  graph are not sent in one write(), there appear some
     *  strange delays between for example the header and
     *  the rest.
     *  
     *  XXX: handle cycles.
     */
   
    public static void write(IGraph g, OutputStream out) throws IOException
    {
    	ByteArrayOutputStream bout = new ByteArrayOutputStream();	
    	final byte[] h = { 1, 'G', 0 };
    	out_write(bout,h);
    	
    	/* This filtering of _NULL should not be necessary, but
    	 * currently is needed at the reception side.
    	 */
    	if (! g.getName().equals(Graph._NULL))
    		_writeBinary(0,g,bout);
    	else
    	    for (int i=0; i<g.size(); i++) {
    		    IGraph node = g.get(i);
    	        _writeBinary(0,node,bout);
    	    }

    	out_write(bout,0);     	
    	bout.writeTo(out);
    }
    
    /* Convert a Graph to binary OGDL and return a byte array */
    
    public static byte[] write(IGraph g) throws IOException
    {
    	ByteArrayOutputStream bout = new ByteArrayOutputStream();	
    	final byte[] h = { 1, 'G', 0 };
    	out_write(bout,h);
    	
       	/* This filtering of _NULL should not be necessary, but
    	 * currently is needed at the reception side.
    	 */
    	if (! g.getName().equals(Graph._NULL))
    		_writeBinary(0,g,bout);
    	else
    	    for (int i=0; i<g.size(); i++) {
    		    IGraph node = g.get(i);
    	        _writeBinary(0,node,bout);
    	    }

    	out_write(bout,0);     	
    	return bout.toByteArray();
    }
    
    private static void _writeBinary(int lev, IGraph g, OutputStream out) throws IOException
    {
		writeTextNode(lev, g.getName(), out);	
    	
		Object b = g.getValue();
		
		if (b!=null && (b instanceof byte[])) 
			writeBinaryNode(lev, (byte[]) b, out );
	
		
    	// do with all nodes
    	IGraph node;
    	
    	for (int i=0; i<g.size(); i++) {
    		node = g.get(i);
    		_writeBinary(lev+1,node,out);
    	}
    }
    
    private static void writeTextNode (int level, String s, OutputStream out) throws IOException
    {
    	/* Empty or null strings will not make it */
    	if (s==null || s.length()==0)
    		return;
    	
       	// write level
    	multiByteInteger(level+1,out); 	
    	// write text
    	out_write(out,s.getBytes("UTF-8"));
    	// write end of text
    	out_write(out,0);     	
    }
    
    private static void writeBinaryNode (int level, byte[] b, OutputStream out) throws IOException
    {
		// XXX split
		multiByteInteger(level+1,out);
		out_write(out,1);
		multiByteInteger(b.length,out);
		out_write(out,b);  
		out_write(out,0);
    }
    
    /* Variable length integer encoding
	 * 
	 * 0 - 0x0000007F:  0xxxxxxx
	 * 0 - 0x00003FFF:  10xxxxxx xxxxxxxx
	 * 0 - 0x001FFFFF:  110xxxxx xxxxxxxx xxxxxxxx
	 * 0 - 0x0FFFFFFF:  1110xxxx xxxxxxxx xxxxxxxx xxxxxxxx
     * 0 - 0x7FFFFFFFF: 11110xxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx
	 * 
	 */

    private static void multiByteInteger(int i, OutputStream out) throws IOException
    {
    	if (i<0x80) { 
    		out_write(out,i); 
    		return; 
    	}
    	if (i<0x4000) {
    		out_write(out,0x80|(i>>8));
    		out_write(out,i&0xff);
    		return;
    	}
    	if (i<0x200000) {
    		out_write(out,0xc0|(i>>16));
    		out_write(out,(i>>8)&0xff);
    		out_write(out,i&0xff);
    		return;
    	}
    	if (i<0x10000000) {
    		out_write(out,0xe0|(i>>24));
    		out_write(out,(i>>16)&0xff);
    		out_write(out,(i>>8)&0xff);
    		out_write(out,i&0xff);
    		return;
    	}  
    	out_write(out,0);
    }

	static void out_write(OutputStream out,int c) throws IOException
	{
//System.out.println("send: "+c+"/"+(char)c);
		out.write(c);
	}
	
	static void out_write(OutputStream out,byte[] b) throws IOException
	{
//for (int i=0; i<b.length; i++)
//System.out.println("send: "+b[i]+"/"+(char)b[i]);

		out.write(b);
	}	

}