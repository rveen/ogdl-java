package ogdl;

import org.junit.*;
import static org.junit.Assert.*;

public class TestSetSubGraph {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test1() throws Exception 
	{
		IGraph g = new Graph();
		g.add("a").add("b");
		g.add("c");
		
		IGraph g2 = new Graph();
		g2.add("a").add("c");
		g2.add("c").add("d");
		g2.add("e");
		
		g.set(g2);
		System.out.println(g);
	}
}
