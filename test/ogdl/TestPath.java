package ogdl;


import org.junit.*;

public class TestPath {

	@Before
	public void setUp() throws Exception {
	}

	@Test 
	public void testPath() throws Exception 
	{
		Path p = new Path("a.b[1]");
		
		while (p.next()){
			System.out.println(p.getElement());
		}
	}
	
	String ex1 = "icon.addr(Q.id \"select * from d where y=\"g\" order by x[1]\" \"responsible\" \"responsible\")";
	
	@Test 
	public void testPath2() throws Exception 
	{
		Path p = new Path(ex1);
		
		System.out.println(p.toString());
	}
}
