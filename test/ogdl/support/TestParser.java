package ogdl.support;


import org.junit.*;

public class TestParser {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test public void quoted() throws Exception 
	{
	    String s = "\'y=\\'a\\'\'";
	    
	    EventHandlerGraph e = new EventHandlerGraph();
	    
	    Parser p = new Parser(s,e);
	    
	    String t = p.quoted(0);
	    
	    System.out.println(t);
	}
}
