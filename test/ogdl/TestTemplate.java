package ogdl;


import java.io.FileWriter;

import org.junit.*;
import static org.junit.Assert.*;
import ogdl.template.*;
import ogdl.support.*;


public class TestTemplate {

	@Before
	public void setUp() throws Exception {
	}

	/** A simple variable has to print its 'contents' and nothing
	 * more than its 'contents'.
	 */
	
	/*
	@Test public void testSimpleVariable() throws Exception
	{	
		IGraph g = new Graph();
		g.add("a").add("b");
		Template t = new Template("$a");

		String s = t.eval(g);
		assertTrue(s.equals("b"));
	}
	
	@Test public void testEmptyVariable() throws Exception
	{	
		IGraph g = new Graph();
		g.add("a");
		Template t = new Template("$a");

		String s = t.eval(g);
		assertTrue(s.length() == 0);
	}
	
	@Test public void testAloneIndex() throws Exception
	{	
		System.out.println("TestAloneIndex");
		IGraph g = new Graph();
		g.add("a");
		Template t = new Template("$[0]");

		String s = t.eval(g);
		System.out.println("  result: "+t);
		assertTrue(s.equals("a"));
	}
	
	@Test public void testIndex() throws Exception
	{	
		IGraph g = new Graph();
		g.add("a").add("b");
		Template t = new Template("$[0]");

		String s = t.eval(g);
		System.out.println("empty: "+s);
		assertTrue(s.equals("b"));
	}
	
	@Test public void testTreeVariable() throws Exception
	{	
		IGraph g = new Graph();
		g.add("a").add("b").add("c");
		Template t = new Template("$a");
	
		String s = t.eval(g);		
		assertTrue(s.equals("b\n  c"));
	}
	
	@Test public void testSet() throws Exception
	{
		Template t = new Template("$(a = 'b')");
		IGraph g = new Graph();
		String s = t.eval(g);
		assertTrue(s.equals("b"));
	}
	
	@Test public void testExtensibleNumbers() throws Exception
	{
		IGraph g = new Graph();
		g.add("a").add("!type").add("java.lang.Math");
		Template t = new Template("$(b = a.sin( 1.0 ))");
		
		String s = t.eval(g);
		assertTrue(s.equals("0.8414709848078965"));
	}

	
	@Test public void testIndex2() throws Exception
	{
		IGraph g = new Graph();
		g.add("a").add("b").add("c");
		Template t = new Template("$a[0][0]");
		
		String s = t.eval(g);
		assertTrue(s.equals("c"));
		
	}
	
	@Test public void testPrintNodeName() throws Exception
	{
		IGraph g = new Graph();
		g.add("a").add("b").add("c");
		Template t = new Template("$a[0]._name");
		
		String s = t.eval(g);
		assertTrue(s.equals("b"));
	}
	
	@Test public void testWordsInPath() throws Exception
	{
		Template t = new Template("$a.b'text'");
		IGraph g = t.graph();
		
		assertTrue("'text'".equals(g.getName(1)));
	}
	 
	@Test public void testSimpleExpression() throws Exception
	{
		IGraph g = new Graph();
		g.add("a").add("!type").add("java.lang.Math");
		g.add("b").add("1.0");
		Template t = new Template("$a.sin( b )");
		
		String s = t.eval(g);	
		
		assertTrue(s.equals("0.8414709848078965"));
	}
	
	@Test public void testOgdlInLine() throws Exception
	{
		IGraph g = new Graph();
		
		Template t = new Template("$('b' 'c')");
		String s = t.eval(g);
		System.out.println(t+"\n"+s);
		
		Template t2 = new Template("$('b', 'c')");
		s = t2.eval(g);
		System.out.println(s);
	}

	*/
	@Test public void testIf() throws Exception
	{
		IGraph g = new Graph();
		String s = "$if(a) X $else $if(b) Y $else Z $end $end";
		
		Template t = new Template(s);
	} 
	
	@Test public void testFor() throws Exception
	{
		IGraph g = new Graph();
		String s = "$for(a,b) aaa $if(a) $end $end";
		
		Template t = new Template(s);
	} 
	
	@Test public void testCase1() throws Exception
	{
		IGraph g = new Graph();
		String s = "$a $for(a,b) $end"; String s2 = "$for(a,rs) id=$Q.id&sid=$s[0].id $end";
		
		Template t = new Template(s);
		System.out.println(t);
		t.eval(g);
		
	} 
	 
	@Test public void testCase2() throws Exception
	{
		IGraph g = new Graph();
		String s = "hola1 $if( \" \" ==  \" \" + Q.id ) $!(Q.id = \"0\") $end hola2";
		
		Template t = new Template(s);
		System.out.println(t);
		String e = t.eval(g);
		System.out.println("----\n"+e);
	}
	
	/*
	@Test public void testCase3() throws Exception
	{
		String s = Util.readFile("/files/projects/intranet/webapp/index.htm");
		Template t = new Template(s);
		System.out.println(t);
		
		FileWriter fo = new FileWriter("/tmp/t2.txt");
		fo.append(t.toString());
		fo.close();
	} */
}

