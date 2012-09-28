package ogdl;


import org.junit.*;
import static org.junit.Assert.*;

import java.util.Vector;

/** Test the OGDL level 2 emmiter:
 * 
 * Graphs with cycles.
 *  
 *  */

public class TestCycle {

	@Before
	public void setUp() throws Exception {
	}
	
	@Test public void testCycle() throws Exception
	{
		IGraph a = new Graph("a");
		IGraph b = new Graph("b");
		
		a.add(b);
		b.add(a);
		
		System.out.println( OgdlEmitter2.toString(a));
	}
	
	@Test public void testCycle2() throws Exception
	{
		IGraph a = new Graph("a");
		IGraph b = new Graph("b");
		IGraph c = new Graph("c");
		IGraph d = new Graph("d");
		a.add(b);
		a.add(c);
		a.add(d).add(c);
		
		System.out.println( OgdlEmitter2.toString(a));
	}
	
	@Test public void testCycle3() throws Exception
	{
		IGraph a = new Graph("a");
		IGraph b = new Graph("b");
		IGraph c = new Graph("c");
		IGraph d = new Graph("d");
		a.add(b);
		a.add(c);
		a.add(d).add(b);
		
		System.out.println( OgdlEmitter2.toString(a));
	}
	
	@Test public void testCycle4() throws Exception
	{
		IGraph a = new Graph("a");
		IGraph b = new Graph("b");
		IGraph c = new Graph("c");
		IGraph d = new Graph("d");
		a.add(b);
		a.add(c);
		a.add(d).add(d);
		
		System.out.println( OgdlEmitter2.toString(a));
	}
	
	@Test public void testCycle5() throws Exception
	{
		IGraph a = new Graph("a");
		IGraph b = new Graph("b");
		IGraph c = new Graph("c");
		IGraph d = new Graph("d");
		a.add(b);
		a.add(c);
		a.add(d).add(a);
		
		System.out.println( OgdlEmitter2.toString(a));
	}
	
	@Test public void testCycle6() throws Exception
	{
		IGraph a = new Graph("a");
		IGraph b = new Graph("b");
		IGraph c = new Graph("c");
		IGraph d = new Graph("d");
		IGraph e = new Graph("e");
		
		a.add(b).add(c);
		a.add(d).add(e);
		e.add(c);
		
		System.out.println( OgdlEmitter2.toString(a));
	}

}
