package ogdl;


import org.junit.*;
import static org.junit.Assert.*;

public class TestParser {

	@Before
	public void setUp() throws Exception {
	}

	@Test 
	public void testComma() throws Exception 
	{
		String s = "a\n  b,c";
		
		IGraph g = Ogdl.parseString(s);
		//System.out.println(g);
	}
	
	@Test 
	public void testCommentInQuoted() throws Exception 
	{
		String s = "a\n  '#b', c";
		
		IGraph g = Ogdl.parseString(s);
		System.out.println(g);
	}
	
	@Test 
	public void testQuoted() throws Exception 
	{
		String s = "\"a\\\"\"";
		String s2 = "\"a\\\"\")";
		
		IGraph g = Ogdl.parseString(s);
		System.out.println(g);
	}
	
	@Test 
	public void testBlock() throws Exception 
	{
		String s = "a\n  \\\n  b\n  )c"; 
		IGraph g = Ogdl.parseString(s);
		System.out.println(g);
		String a = g.getString("a");
		assertEquals(a,"b\n)c");
	}
	
	@Test 
	public void testBlock2() throws Exception 
	{
		System.out.println("---\ntestBlock2:");
		
		IGraph g = new Graph("a");
		g.add("1");
		g.add("b\nc");
		g.add("d");
		
		String s = g.toString();
		System.out.println(g);
		IGraph out = Ogdl.parseString(s);
		IGraph e = out.get(0).get(1);
		System.out.println(e);
		String a = out.getString("a.[1]");
		assertEquals(a,"b\nc");
	}
	
	@Test public void testBlock3() throws Exception
	{
		System.out.println("---\ntestBlock3:");
		String s="Description\n  !text\n  name object.x[2]\n  value\n   \\\n" +
		         "   ! Purpose / Objectives\n" +
		         "   The User Requirements describe the user-level aspects of the system.";
	
	
		IGraph out = Ogdl.parseString(s);
		System.out.println(out);
		String val = out.getString("[0].value");
		System.out.println(val);
	}
	
	@Test public void testStrangeTokens() throws Exception
	{
		System.out.println("---\ntestStrangeTokens:");
		
		String s = "a ( b c, d )";
		IGraph out = Ogdl.parseString(s);
		System.out.println(out);
		
		s = "office:document-content ( xmlns:office 'urn', xmlns:style 'urn:oasis:names:tc:opendocument:xmlns:style:1.0' )";
		out = Ogdl.parseString(s);
		System.out.println(out);
	}
	
	@Test public void testBlock4() throws Exception
	{
		System.out.println("---\ntestBlock4:");
		String s = "x2asdfasdf\n  \\\n    ! Description\n   Text\n   More text";

		IGraph out = Ogdl.parseString(s);
		System.out.println(s+"\n---\n"+out);
	}
}
