package ogdl.support;

import java.util.Vector;

public class EventHandlerList implements IEventHandler
{
	private int    level = 0;
	public Vector<String> v = null;
	
	public EventHandlerList()
	{
	}
	
	/** Increase level */
	public int inc() 
	{ 
		level++; 
		return level;
	}
	
	/** Decrease level */
	public int dec() 
	{ 
		if (level>0) 
			level--;
		return level;
	}
	
	/** Get level */
	public int level()
	{
		return level;
	}
	
	/** Set level */
	public void level(int l)
	{
		level = l;
	}
	
	/** Clear the event tree */
	public void clear()
	{
	}
	
	public void event(int type)
	{
	}
	
	public void event(String s)
	{
		if (v==null)
			v = new Vector<String>();
		v.add(s);
	}
	
	public void event(byte[] data)
	{	
	}
	   
    public void error(Exception e, int line)
    {
    }
    
    public String toString()
    {
    	StringBuilder sb = new StringBuilder();
    	for (int i=0; i<v.size(); i++) {
    		sb.append(v.get(i));
    		sb.append('\n');
    	}
    	return sb.toString();
    }
}
