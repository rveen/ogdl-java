package ogdl.template;

import org.junit.*;
import static org.junit.Assert.*;
import ogdl.*;

public class TestEval 
{
	@Test public void testSimpleVariable() throws Exception
	{	
		IGraph g = new Graph();
		g.add("a").add("b");
		g.add("b").add("x");
		Template t = new Template("$$a");
		
		Object o = Evaluate2.eval(t.g,g);
		
		assertEquals(o,"x");
	}
	
	@Test public void testString() throws Exception
	{
		IGraph g = new Graph();
		g.add("a").add("01");

		Template t = new Template("$(''+a)");
		
		Object o = Evaluate2.eval(t.g,g);
		
		System.out.println("---\n"+o+"\n");
		
		assertEquals(o.getClass().getName(),"java.lang.String");
		assertEquals(o,"01");
	}
	
	@Test public void testNumber() throws Exception
	{
		IGraph g = new Graph();
		g.add("a").add("01");

		Template t = new Template("$(2+a)");

		Object o = Evaluate2.eval(t.g,g);
		
		assertEquals(o.getClass().getName(),"java.lang.Long");
		assertEquals(o,3L);
	}
	
	@Test public void testNumber2() throws Exception
	{
		IGraph g = new Graph();
		g.add("a").add("01");

		Template t = new Template("$(2*a)");
		
		Object o = Evaluate2.eval(t.g,g);
		
		assertEquals(o.getClass().getName(),"java.lang.Long");
		assertEquals(o,2L);
	}
	
	@Test public void testAddString() throws Exception
	{
		IGraph g = new Graph();

		Template t = new Template("$('a'+'01')");
		
		Object o = Evaluate2.eval(t.g,g);
		
		assertEquals(o.getClass().getName(),"java.lang.String");
		assertEquals(o,"a01");
	}
	
	@Test public void testAddString2() throws Exception
	{
		IGraph g = new Graph();
		g.add("a").add("01");

		Template t = new Template("$('a'+a)");
		
		Object o = Evaluate2.eval(t.g,g);
		
		assertEquals(o.getClass().getName(),"java.lang.String");
		assertEquals(o,"a01");
	}
	
	@Test public void testEquals1() throws Exception
	{
		IGraph g = new Graph();
		g.add("a").add("'01'");
		g.add("b").add("1");

		Template t = new Template("$(a==b)");
		
		Object o = Evaluate2.eval(t.g,g);
		
		System.out.println(o);
	}
	
	@Test public void testEquals2() throws Exception
	{
		IGraph g = new Graph();
		g.add("a").add("01");
		g.add("b").add("1");

		Template t = new Template("$(b==a)");
		
		Object o = Evaluate2.eval(t.g,g);
		
		assertEquals(o.getClass().getName(),"java.lang.Boolean");
		assertEquals(o,true);
	}
	
	@Test public void testAssign2() throws Exception
	{
		IGraph g = new Graph();
		IGraph a = g.add("a");
		a.add("x");
		a.add("y");

		Template t = new Template("$!(a[0].f='Z') $a");
		
		System.out.println(t);
		
		Object o = Evaluate2.eval(t.g,g);
		
		//assertEquals(o.getClass().getName(),"java.lang.Boolean");
		//assertEquals(o,true);
		System.out.println("---\n"+o+"\n---");
	}

	@Test public void testNumericFor() throws Exception
	{
		IGraph g = new Graph();
		g.add("n","3");
		
		Template t = new Template("$for(i,n) $i $end");
		System.out.println(t);
		String s = t.eval(g);
		System.out.println("---\n"+s+"\n---");
	}
	
	@Test public void testDolar() throws Exception
	{
		IGraph g = new Graph();
		Template t = new Template("$\\[!]");
		System.out.println(t);
		String s = t.eval(g);
		System.out.println("---\n"+s+"\n---");
	}
	
	@Test public void testCheck1() throws Exception
	{
		IGraph g = new Graph();
		g.add("a").add("1");
		g.add("b").add("2");

		Template t = new Template("$(a==1)");
		
		boolean b = Evaluate2.evalBoolean(t.g,g);
		assertEquals(b,true);
		
		t = new Template("$(a>0)");
		b = Evaluate2.evalBoolean(t.g,g);
		assertEquals(b,true);
		
		t = new Template("$(b==1)");
		b = Evaluate2.evalBoolean(t.g,g);
		assertEquals(b,false);
		System.out.println(t.g);
	}
}
