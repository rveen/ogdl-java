package ogdl.support;

import java.io.IOException;
import java.util.Comparator;

import ogdl.IGraph;
import ogdl.IPath;
import ogdl.Path;
import ogdl.SyntaxException;

/** THis is a helper class for implementing sort() */

public class GraphComparator implements Comparator
{
	IPath ipath; 
	String path;
	boolean asc;
	boolean numeric=false;
	boolean ignoreCase=true;
	
	public GraphComparator(String path, boolean ascending) throws IOException, SyntaxException
	{
		ipath = new Path(path);
		asc = ascending;
		this.path = path;
	}
	
	public GraphComparator(String path, boolean ascending, boolean numeric, boolean ignoreCase) throws IOException, SyntaxException
	{
		ipath = new Path(path);
		asc = ascending;
		this.path = path;
		this.numeric = numeric;
		this.ignoreCase = ignoreCase;
	}
	
	public GraphComparator(String path, boolean ascending, boolean numeric) throws IOException, SyntaxException
	{
		ipath = new Path(path);
		asc = ascending;
		this.numeric = numeric;
		this.path = path;
	}
	
	public int compare(Object o1, Object o2) 
	{	    
	    try {
	    	IGraph g1 = (IGraph) o1;
		    IGraph g2 = (IGraph) o2;
		    int i=0;
		    
	    	String p1 = g1.getString(path); // Use IPath
			String p2 = g2.getString(path);
			
			p1 = ogdl.support.Util.unquote(p1);
			p2 = ogdl.support.Util.unquote(p2);
			
			if (numeric) {
				if (p1==null) 
					i = -1;
				else if (p2==null)
					i= 1;
				else {
					long i1 = Long.parseLong(p1);
					long i2 = Long.parseLong(p2);
					if (i1 < i2)
						i = -1;
					else if (i1 > i2)
						i = 1;
				}
			}
			else {
				if (p1==null)
					p1="";
				if (p2==null)
					p2="";
				i = ignoreCase? p1.compareToIgnoreCase(p2) : p1.compareTo(p2);
			}
			
			return asc? i:-i;
		} catch (Exception e) {
			// System.out.println("path not found in one of the comparing objects: "+path);
			// e.printStackTrace();
			return -1;
		}
	}

	public boolean equals(Object o) 
	{
	    return this == o;
	}
}