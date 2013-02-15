package ogdl.template;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestPathToOGDL {

	@Test public void test1() throws Exception
	{	
		String s = "$svn.list('hola'+'4')";
		Template t = new Template(s);
		System.out.println(t);
	}
	
	@Test public void test2() throws Exception
	{	
		String s = "$svn.list.(a.r=b)";
		Template t = new Template(s);
		System.out.println(t);
	}
}
