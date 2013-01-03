/* OGDL, Ordered Graph Data Language 
 * (c) R.Veen, 2005-2010.
 * License: zlib (see http://ogdl.org/license.htm)
 */

package ogdl;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import ogdl.support.ByteBuffer;
import ogdl.support.IEventHandler;

import java.util.logging.Logger;


/**
 * Parser for the OGDL binary format
 * 
 * date: Nov 2005.
 * 
 * XXX Detect client closing connection!!
 */

public final class OgdlBinaryParser 
{
	public final static int CONTENT = 1; /* primary events, content related */
	public final static int FORMAT  = 2; /* secondary events, format related */
	public final static int BINARY  = 3; /* secondary events, format related */

	protected InputStream in = null;
	protected RandomAccessFile inr = null;
	boolean raw = false;

	static Logger log = Logger.getLogger(OgdlBinaryParser.class.getName());
	
	int line = 1, level = -1, groups[], lineInd[], lineLevel, groupLevel,
			groupIndex = 0;

	private IEventHandler event;

	ByteBuffer bb;

	public OgdlBinaryParser(InputStream in, IEventHandler event) throws Exception {
		this.in = in;
		this.event = event;
		groups = new int[64];
		lineInd = new int[64];

		bb = new ByteBuffer(0);
	}

	public OgdlBinaryParser(RandomAccessFile inr, IEventHandler event)
			throws Exception {
		this.inr = inr;
		raw = true;
		this.event = event;
		groups = new int[64];
		lineInd = new int[64];

		bb = new ByteBuffer(0);
	}

	boolean header() throws Exception 
	{
		/* Read two bytes (0x0147) */
		int c;
		
		if ((c=read()) != 0x01)
			//return false;
			throw new Exception("header not found: "+c);
		if ((c=read()) != 0x47)
			//return false;
		    throw new Exception("header not found: "+c);
		while (read() != 0x00)
			;

		return true;
	}

	/*
	 * Variable length integer encoding
	 * 
	 * 0 - 0x0000007F: 0xxxxxxx 
	 * 0 - 0x00003FFF: 10xxxxxx xxxxxxxx 
	 * 0 - 0x001FFFFF: 110xxxxx xxxxxxxx xxxxxxxx 
	 * 0 - 0x0FFFFFFF: 1110xxxx xxxxxxxx xxxxxxxx xxxxxxxx 
	 * 0 - 0x7FFFFFFFF: 11110xxx xxxxxxxx xxxxxxxx xxxxxxxx xxxxxxxx
	 * 
	 * This function reads byte by byte to compose the full integer.
	 */

	int integer() throws IOException {
		int b0, b1, b2, b3;

		b0 = read();
		if (b0 < 0x80)
			return b0;
		
		if (b0 < 0xc0) {
			b1 = read();
			return (b0 & 0x3f) << 8 | b1;
		}
		
		if (b0 < 0xe0) {
			b1 = read();
			b2 = read();
			return ((b0 & 0x1f) << 16) | (b1 << 8) | b2;
		}
		
		if (b0 < 0xf0) {
			b1 = read();
			b2 = read();
			b3 = read();
			return ((b0 & 0x0f) << 24) | (b1 << 16) | (b2 << 8) | b3;
		}

		// XXX
		return 0;
	}

	boolean line() throws IOException 
	{
		/* read a multibyte integer, indicating the level */
		
		int lev = integer() - 1;
		if (lev == -1)
			return false;

		/* text or binary ? */
		int c = read();

		if (c == 1) {

			/*
			 * binary node. Composed by an arbitrary number of chuncks
			 */

			int len;

			bb.reset();

			while ((len = integer()) > 0) {
				
				bb.extend(len);
				for (int i = 0; i < len; i++) {
					bb.put((byte) read());
				}
			}
			
			event.level(lev);
			event.event(bb.clone());
			return true;
		} 
		else if (c > 1) 
		{
			/* text node */

			bb.reset();
			bb.extend(64);
			bb.put((byte) c);

			while ((c = read()) != 0)
				bb.put((byte) c);

			event.level(lev);
			event.event(new String(bb.getBuffer(), 0, 
					bb.length(), "UTF-8"));

			return true;
		}
		else {
			event.level(lev);
			event.event("");
		}
		
		return true;
		
		/* XXX: If we return true here, then null values can be transferred,
		 * and thus empty nodes. Check this.
		 * (Empty (null) nodes are used to represent lists)
		 */
		//return false;
	}

	public void parse() throws Exception 
	{	
		if (!header()) {				
			log.warning("No header / EOS");
			return;
		}
		log.finest("Header received");
		
		while (line())
			;
	}
	
	/** XXX this function may/should substitute parse() */
	
	public boolean parse2() throws Exception 
	{
		if (!header()) {	
			log.warning("No header / EOS");
			return false;
		}
		log.finest("Header received");
		
		while (line())
			;
		return true;
	}

	/* Small isolation layer */

	private int read() throws IOException 
	{
		int c;
		
		if (raw)
			c = inr.read();
		else
			c = in.read();
		
// System.out.println("rec: "+c+"/"+(char)c);
		return c;
	}

}
