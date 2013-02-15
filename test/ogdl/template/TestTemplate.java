

package ogdl.template;

import org.junit.*;
import static org.junit.Assert.*;
import ogdl.*;
import ogdl.template.Template;

public class TestTemplate 
{
	@Test public void test() throws Exception
	{	
		String s = "$svn.list";
		Template t = new Template(s);
		System.out.println(t);
	}
}
