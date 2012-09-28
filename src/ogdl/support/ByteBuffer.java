package ogdl.support;

public class ByteBuffer 
{
    byte[] buffer=null;
    int len=0;
    int pos=0;
    
    public ByteBuffer() 
    {
        buffer = new byte[16];	
        this.len = 16;
    }
    
    public ByteBuffer(int len) 
    {
        buffer = new byte[len];	
        this.len = len;
    }    
    
	/** add a byte to the buffer */
    
	public void put(byte b) 
	{
		if (pos>=len) 
			extend(16);
		buffer[pos++] = b;
	}
	
	/** extend the local buffer by an amount */
	
	public void extend(int len) 
	{
		byte[] a = new byte[this.len+len];
		
		for (int i=0; i<this.len; i++)
			a[i] = buffer[i];
		
		buffer = a;
		this.len+=len;
	}
	
	/** reset the internal buffer to zero.
	 *  This way it can be reused.
	 */
	
	public void reset()
	{
		buffer = null;
		len = 0;
		pos = 0;
	}
	
	/** return the number of bytes in the buffer */
	
	public int length()
	{
		return pos;
	}
	
	/** return the buffer */
	
    public byte[] getBuffer()
    {
    	return buffer;
    }
    
	/** return the buffer */
    
    public byte[] clone()
    {
    	byte[] a = new byte[len];
    	
    	for (int i=0; i<len; i++)
    		a[i]=buffer[i];
    	
    	return a;
    }    
}
