package ogdl;


import org.junit.*;
import static org.junit.Assert.*;

import java.util.Vector;

public class TestGraph {

	@Before
	public void setUp() throws Exception {
	}
	
	@Test public void testIndex() throws Exception
	{
		IGraph g = new Graph();
		IGraph a = g.add("a");
		a.add("b");
		a.add("c");
		
		String s = g.getString("a[0]");
		
		assertEquals(s,"b");
	}
	
	@Test public void testGetStringEmpty() throws Exception
	{
		IGraph g = new Graph();
		IGraph a = g.add("a");
		
		String s = g.getString("a");
		
		System.out.println("2:" + s);
		assertEquals(s,null);
	}
	
	/** Here we test setting/getting of the 3 types of objects:
	 *  
	 * CharSequence   converted to a regular node.
	 * IGraph         added to the graph (with or without root)
	 * Other objects  special _OBJ node with value set 
	 */
	
	@Test
	public void testSetGet() throws Exception
	{
		/* set (path, object) 
		 * object = get (path)
		 * 
		 * Case: String
		 */
		
		IGraph g = new Graph();
		g.set("a", "b");
		
		Object o = g.get("a");
		assertEquals(o.getClass().getName(),"java.lang.String");
		
		/* Case: Other CharSequences */
		StringBuilder s = new StringBuilder();
		s.append("pepe");
		
		g.set("b",s);
		o = g.get("b");
		assertEquals(o.getClass().getName(),"java.lang.String");
		
		/* Case: IGraph, literal path */
		g.add("p").add("q").add("r");
		
		o = g.get("p");
		assertEquals(o.getClass().getName(),"ogdl.Graph");
		
		/* Case: IGraph, path with index */
		
		o = g.get("[2]");
		assertEquals(o.getClass().getName(),"ogdl.Graph");
		
		/* Case: Arbitrary objects */
		
		Vector<String> v = new Vector<String>();
		v.add("x");
		v.add("y");
		
		g.set("c", v);
		
		o = g.get("c");
		assertEquals(o.getClass().getName(),"java.util.Vector");
	}

	@Test
	public void testEquals() throws Exception
	{
		IGraph g1 = new Graph();
		IGraph g2 = new Graph();
		IGraph g3 = new Graph("a");
		
		g1.add("a").add("b");
		
		g2.add("a").add("b");
		
		g3.add("b");
		
		assertTrue(g1.equals(g2));
		
		assertTrue(!g1.equals(g3));
	}
	
	@Test
	public void testPrint() throws Exception
	{
		/* Check that toString() prints the 'root' object if not null.
		 */
		
		IGraph g1 = new Graph();
		IGraph g2 = new Graph("b");
		g1.add("a");
		g2.add("a");

		assertTrue(g1.toString().equals("a"));
		assertTrue(g2.toString().equals("b\n  a"));
	}
	
	@Test
	public void testStringAndBack() throws Exception
	{
		String s = "a 'b b'\nc 'd d'\ne f, g\nx";
		IGraph g = Ogdl.parseString(s);
		System.out.println(g.toString());
	}
	
	@Test
	public void testParenthesisInText() throws Exception
	{
		String s = "a 'b(x)c'";
		IGraph g = Ogdl.parseString(s);
		System.out.println(g.toString());
	}
	
	@Test
	public void testPrintNull() throws Exception
	{
		IGraph g = new Graph();
		g.add("p");
		String s = ""+g;
		assertTrue(s.equals("p"));
	}
	
	@Test
	public void testAfterParenthesis() throws Exception
	{
		String s = "a b(c, d)e";
		IGraph g = Ogdl.parseString(s);
		System.out.println(g.toString());
	}

}
