package ogdl;

import org.junit.Before;
import org.junit.Test;


public class TestEmitter {
	@Before
	public void setUp() throws Exception {
	}

	@Test 
	public void testBlock() throws Exception 
	{
		IGraph g = new Graph("a");
		g.add("b\nc");
		
		System.out.println(g);
	}
	
	@Test 
	public void testBlockInArray() throws Exception 
	{
		IGraph g = new Graph("a");
		g.add("b\nc");
		g.add("d");
		
		System.out.println(g);
	}
	
	@Test 
	public void testBlockInArray2() throws Exception 
	{
		IGraph g = new Graph("a");
		g.add("1");
		g.add("b\nc");
		g.add("d");
		
		System.out.println(g);
	}
}
