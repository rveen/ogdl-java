/* OGDL, Ordered Graph Data Language 
 * (c) R.Veen, 2002-2006.
 * License: see http://ogdl.org/ (similar to zlib)
 */

package ogdl.support;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.StringTokenizer;
import java.util.Comparator;

import ogdl.Graph;
import ogdl.IGraph;
import ogdl.SyntaxException;

/** Graph utilities
 * 
 * $Id$
 */

public final class Graphs 
{
    /** sort a Graph */
	
	public static IGraph sort (IGraph g, String path, boolean asc) throws IOException, SyntaxException
	{
		return sort(g,path,asc,false);
	}
	
	public static IGraph sort (IGraph g, String path, boolean asc, boolean numeric) throws IOException, SyntaxException
	{
		if (g == null) return null;
		
		// Convert to a array
		Object[] ag = g.toArray();
		if (ag == null) return null;
		
		// Sort
		Comparator c = new GraphComparator(path, asc, numeric);
		java.util.Arrays.sort(ag, c);
		
		// Convert back to Graph
		IGraph r = new Graph();
		for (int i=0; i<ag.length; i++)
			r.add((IGraph) ag[i]);
		
		return r;
	}
		
    
    /** Returns an exception as Graph. Usefull for logging */
    
    public static IGraph get(Throwable ex) 
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String s = sw.toString();
        StringTokenizer st = new StringTokenizer(s,"\n");
        
        Graph g;
        
        if (ex instanceof Exception)
            g = new Graph("exception");
        else
            g = new Graph("throwable");
        
        IGraph node = g.add(""+ex);
        
        int index = 0;
        while (st.hasMoreElements()) {
            index++;
            if (index == 1) { st.nextToken(); continue; }
            String t = st.nextToken().trim(); //.substring(3);
            node.add(t);
        }
        return g;
    }
    
    
    public static IGraph get_(Throwable ex) {
        StackTraceElement[] e;
        
        e = ex.getStackTrace();
        
        Graph g;
        
        if (ex instanceof Exception)
            g = new Graph("exception");
        else
            g = new Graph("throwable");
        
        // g.setValue(ex.toString());
        g.add( (IGraph) new Graph( ex.toString() ) );
        
        for (int index = 0; index<e.length; index++)
            g.add( (IGraph) new Graph( e[index].toString() ) );
        
        return g;
    }
    
        
    /*
     * Return a copy of the input graph, filtered for
     * the given user/group permission list.
     * 
     * Nodes that have a subnode(0) with name "!acl" are
     * checked against the user/group list.
     * 
     * 
     */
    
    public static IGraph filterByAcl(IGraph in, IGraph access)
    {
    	IGraph g = (IGraph) in.clone();
    	return filterByAcl_(g,access);
    }
    
    static IGraph filterByAcl_(IGraph g, IGraph access)
    {	
    	if (g.size()>0) {
    		String name = g.get(0).getName();
    		if (name.equals("!acl")) {
    			if (!hasAccess(g.get(0),access)) { 				
    				g.remove();				
    			}
    		}
    	}
    	
    	for (int i=0; i<g.size(); i++)
    		filterByAcl_(g.get(i),access);
    	
    	return g;
    }
    
    /**
     * Returns true if access contains at least one element of g.
     */
    private static boolean hasAccess(IGraph g, IGraph access)
    {
    	for (int i=0; i<g.size(); i++) {
    		for (int j=0; j<access.size(); j++) {
    			String a = g.get(i).getName();
    			String b = access.get(j).getName();
 			
    			if (a.equals(b))
    				return true;
    		}
    	}
    	return false;
    }
      
    
    /** Add nodes from a string array 
     * 
     * @deprecated
     * */
    
    public static void add(IGraph g, String[] ss)
    {
    	for (int i=0; i<ss.length; i++)
    		g.add(ss[i]);
    }
}
