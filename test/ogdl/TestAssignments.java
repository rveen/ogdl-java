package ogdl;

import org.junit.*;
import ogdl.template.*;
import static org.junit.Assert.*;

public class TestAssignments {
	
	@Before
	public void setUp() throws Exception {
	}
	
	/*@Test*/ public void testString() throws Exception
	{
	    IGraph g = new Graph();
	    Template t = new Template("$(a='1')");
	    String s = t.eval(g);
	    System.out.println(g);
	}
	
	/*@Test*/ @Test public void testString2() throws Exception
	{
	    IGraph g = new Graph();
	    Template t = new Template("$!(a='')$a");
	    String s = t.eval(g);
	    System.out.println(s+"\n--\n"+t);
	    assertEquals("",s);
	}
	
	/*@Test*/ public void testNumber() throws Exception
	{
	    IGraph g = new Graph();
	    Template t = new Template("$(a=1)");
	    String s = t.eval(g);
	    System.out.println(g);
	}

	@Test public void testSetName() throws Exception
	{
		System.out.println("testSetName");
	    IGraph g = new Graph();
	    IGraph node = g.add("a");
	    node.add("b");
	    node.add("c");
	    Template t = new Template("$!(a._name='A')");
	    String s = t.eval(g);
	    System.out.println(g);
	    assertEquals("",s);
	}
}
