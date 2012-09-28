package ogdl;


import org.junit.*;
import static org.junit.Assert.*;
import ogdl.template.*;


public class TestComplexPath {

	@Before
	public void setUp() throws Exception {
	}

	/** Check that the produced graph can be evaluated, and is as simple
	 * as possible.
	 */
	
	@Test public void test1() throws Exception
	{	
		IGraph g = new Graph();
		g.add("a").add("b");
		
        ComplexPath e = new ComplexPath("a.b");
		assertTrue(g.equals(e.graph()));
		e = new ComplexPath("'a'.'b'");
		assertTrue(g.equals(e.graph()));
	}
		
	@Test public void test2() throws Exception
	{
        ComplexPath e = new ComplexPath("a('b')");
		
		System.out.println(e);
		
		e = new ComplexPath("a.q('a' + 'b')");
		
		System.out.println(e);
	}
	
    /* XXX $process.process(t[0].pid).title
     * 
     * Continuar despues de argumentos!
     */
     
	
}
