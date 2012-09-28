package ogdl;

import java.io.IOException;

import ogdl.*;
import ogdl.support.*;
import org.junit.*;
import static org.junit.Assert.*;

public class TestSort 
{
	@Test public void sort1() throws Exception
	{
		IGraph g = new Graph();
		g.add("pepe");
		g.add("pepé");
		g.add("ana");
		g.add("paqui");
		
		IGraph r = Graphs.sort(g, "_name", true);
		System.out.println(r);
	}
	
	@Test public void sort2() throws Exception
	{
		IGraph g = new Graph();
		IGraph a1 = new Graph("1");
		g.add(a1).add("x0").add("a");
		
		IGraph a2 = new Graph("2");
		g.add(a2).add("x0").add("c");
		
		IGraph a3 = new Graph("3");
		g.add(a3).add("x0").add("b");
		
		IGraph r = Graphs.sort(g, "x0", true);
		System.out.println(r);
	}
}
