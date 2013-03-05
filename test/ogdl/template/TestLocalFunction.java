package ogdl.template;

import org.junit.*;

import static org.junit.Assert.*;
import ogdl.*;
import ogdl.support.Types;

public class TestLocalFunction 
{
	@Test public void test1() throws Exception
	{	
		LocalFunction lf = new LocalFunction("java.lang.Math");
		
		IGraph g = new Graph();
		g.add("math").setValue(lf);
		g.add("math2").add(Types.OBJECT).setValue(lf);
		
		Template t = new Template("$math2.max(1, 2)");
		
		Object o = Evaluate.eval(t.g,g);
		
		System.out.println(o);
		//assertEquals(o,"x");
	}

	@Test public void test2() throws Exception
	{	
		Template t = new Template("$a.b(1, 2).c");
		
		System.out.println(t);
	}
	
	@Test public void test3() throws Exception
	{	
		IGraph g = new Graph();
		
		IGraph a = g.add("a");
		IGraph b = g.add("b");
		
		a.add("x");
		a.add("y");
		
		b.add("w");
		b.add("z");
		
		LocalFunction lf = new LocalFunction("java.lang.Math");
		g.add("math").setValue(lf);
		
		Template t = new Template("$math.sin(a, b)");
		
		Object o = Evaluate.eval(t.g,g);
		
		System.out.println(o);
	}
	
	@Test public void testGraphArguments() throws Exception
	{
		IGraph g = new Graph();
		
		IGraph a = g.add("a");
		IGraph b = g.add("b");
		
		a.add("x");
		a.add("y");
		
		b.add("w");
		b.add("z");
		
		LocalFunction lf = new LocalFunction("ogdl.support.Graphs");
		g.add("graph").setValue(lf);
		
		Template t = new Template("$graph.sort(a, 'a', 'true')");
		
		System.out.println(t);
		Object o = Evaluate.eval(t.g,g);
	}
}
