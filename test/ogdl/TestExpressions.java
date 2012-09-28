package ogdl;


import static org.junit.Assert.assertTrue;
import ogdl.template.Template;

import org.junit.Test;

public class TestExpressions {

	@Test public void testOr() throws Exception
	{
		IGraph g = new Graph();
		g.add("a").add("'x'");
		g.add("b").add("'y'");
		g.add("c").add("'z'");
		Template t = new Template("$(a==b || a==c)");
		System.out.println(t);
		String s = t.eval(g);
		
		System.out.println(s);
	}
	
	@Test public void test2() throws Exception
	{
		Template t = new Template("$a(b+c, d)");
		System.out.println(t);
	}

}
