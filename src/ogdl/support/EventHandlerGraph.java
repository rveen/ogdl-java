package ogdl.support;

/** This is a basic event handler that 
 * builds a tree (in a Graph object)
 */

import ogdl.*;

public class EventHandlerGraph implements IEventHandler 
{
	private int level = 0;
	private Graph g = new Graph();
	IGraph[] v = new IGraph[256]; // XXX

	public EventHandlerGraph() {
		v[0] = g;
	}

	/** Increase level */
	public int inc() {
		level++;
		return level;
	}

	/** Decrease level */
	public int dec() 
	{
		if (level > 0)
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
		g = new Graph();
		level = 0;
	}

	public void event(String s) 
	{
		//if (s == null || s.length() == 0)
			//return;
		
//System.out.println("event: "+s);       
		if (level>200) { 
			System.out.println("EventHandlerGraph: ! level=200 at "+s);
			//return;
			throw new RuntimeException("FATAL: EventHandlerGraph: ! level=200\n"+v[0]);
		}
		
		if (v[level] == null) {
			System.out.println("EventHandlerGraph: ! level step at "+level);
			throw new RuntimeException("FATAL: EventHandlerGraph: v[level] = null at level "+level);
			//return;
		}
	
		Graph node = new Graph(s);
		v[level].add(node);
		v[level + 1] = node;
	}

	public void event(byte[] data) {
		Graph node = new Graph(Graph._OBJ);
		node.setValue(data);
		v[level].add(node);
		v[level + 1] = node;
	}

	public void error(Exception e, int line) {
		IGraph node = g.add("error");
		node.add("text").add("" + e);
		node.add("line").add("" + line);
	}

	public IGraph get() {
		return g;
	}

}
