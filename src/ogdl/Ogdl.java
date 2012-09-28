/* OGDL, Ordered Graph Data Language 
 * (c) R.Veen, 2002-2010.
 * License: zlib (see http://ogdl.org/license.htm)
 */

package ogdl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.net.URL;

import ogdl.support.*;

/** This class contains some easy functions to parse OGDL (convert text or binary OGDL to
 * a Graph object). 
 */

public class Ogdl
{
    /** Read an OGDL file and return it as Graph */
    
	public static IGraph parse(String file) throws Exception
    {
		Reader reader = null;
		
		/* if name contains :// asume URL */
		
		if (file.indexOf("://")!=-1) {
			URL url = new URL(file);
			reader = new InputStreamReader(url.openStream(),"UTF-8");
		}
		else {
			File f = new File(file);
			if (!f.canRead())
				throw new IOException("File not found or unreadable");
        
        	reader = new InputStreamReader(new FileInputStream(f),"UTF-8");
		}
		
        EventHandlerGraph handler = new EventHandlerGraph();

        OgdlParser p = new OgdlParser(reader,handler);
        p.parse();
        reader.close();
        return handler.get();
    }
	
	/** Read an OGDL string and return it as Graph */
	
    public static IGraph parseString(String s) throws Exception
    {
    	if (s == null) return null;
    	
        Reader reader = new StringReader(s);


        EventHandlerGraph handler = new EventHandlerGraph();

        OgdlParser p = new OgdlParser(reader,handler);
        p.parse();
        reader.close();
        return handler.get();
    } 
    
    /** Read OGDL text from an InputStream and return it as Graph */
    
    public static IGraph parse(InputStream in) throws Exception
    {
        Reader reader = new java.io.InputStreamReader(in);

        EventHandlerGraph handler = new EventHandlerGraph();

        OgdlParser p = new OgdlParser(reader,handler);
        p.parse();
        reader.close();
        return handler.get();
    }    
    
    /** Parse a binary OGDL stream. 
     * 
     * This function does not close the input stream
     */
    
    public static IGraph parseBinary (InputStream in) throws Exception
    {
        EventHandlerGraph handler = new EventHandlerGraph();  
        
      	OgdlBinaryParser p = new OgdlBinaryParser(in,handler);
       	p.parse();       	
       	return handler.get();
    }  
    
    /** Parse a binary OGDL stream from a RandomAccessFile. 
     */
    
    public static IGraph parseBinary (RandomAccessFile in) throws Exception
    {
        EventHandlerGraph handler = new EventHandlerGraph();
       	OgdlBinaryParser p = new OgdlBinaryParser(in,handler);
       	p.parse();     	
       	return handler.get();
    } 
    
    /** Parse a binary OGDL stream from a byte buffer */
    
    public static IGraph parseBinary (byte[] buf) throws Exception
    {
    	ByteArrayInputStream in = new ByteArrayInputStream(buf);
    	
        EventHandlerGraph handler = new EventHandlerGraph();
       	OgdlBinaryParser p = new OgdlBinaryParser(in,handler);
       	p.parse();     	
       	return handler.get();
    }  

}