package ogdl.misc;

import org.junit.*;
import ogdl.*;

public class TestRf {

	@Before
	public void setUp() throws Exception 
	{}
	
	@Test
	public void testVersion() throws Exception
	{
		RFClient rf = new RFClient("localhost",1111);
		
		long t0 = System.currentTimeMillis();
		
		Object g=null;
		
		int N = 10000;
		
		for (int i=0; i<N; i++)
		    g = rf.exec(new Graph("version"));
		
		System.out.println(g);
		
		long t1 = System.currentTimeMillis();
		System.out.println("tdiff "+(double)(t1-t0)/N + " ms");
	}

}
