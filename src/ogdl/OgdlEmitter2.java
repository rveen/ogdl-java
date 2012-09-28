/* OGDL, Ordered Graph Data Language 
 * (c) R.Veen, 2002-2010.
 * License: zlib (see http://ogdl.org/license.htm)
 */

package ogdl;
import java.util.HashMap;

import ogdl.IGraph;

/** This emitter is for level 2 (cycles).
 * 
 * It should later be merged with OgdlEmitter.
 * 
 * [!] Cycles are costly: A hashmap is used to remember which nodes are
 * already printed. 
 * 
 * XXX Pending:
 * 
 * - What if value != null ?
 */

public final class OgdlEmitter2 
{  
    public static String toString(IGraph g)
    {
        StringBuffer sb = new StringBuffer();
        HashMap<IGraph,Integer> h = new HashMap<IGraph,Integer>();
        
        addNodes(sb,g,0,h,0);

        return sb.toString();
    }
    
    /* ---- private section ---- */
    
    private static int addNodes(StringBuffer sb, IGraph g, int level, HashMap<IGraph,Integer> h, int y) 
    {
    	if ( h.containsKey(g) )
    	{
    		Integer p = h.get(g);
    		
    		addString(sb,"#{"+(y-p.intValue())+"",level);
    		return y+1;
    	}
    	else
    		h.put(g,new Integer(y));
    	
    	if (g.getName() != null && !g.getName().equals(Graph._NULL)) {
            addString(sb,g.getName(),level);
            y++;
    	}
    	
    	/* XXX What if both, value and nodes are present ? */
    	
    	if (g.getValue()!=null) 
    	{
    		addString(sb,g.getValue().toString(), level+1);
    		return y+1;
    	}
    	
        for (int i=0; i<g.size(); i++) 
        {
            IGraph node = g.get(i);
            y = addNodes(sb,node,level+1,h,y);
        }
        return y;
    }
    
    private static void addString(StringBuffer sb, String s, int level) 
    {
        if (s == null) return;
        
        int n = level * 2;
        
        
        if (s.indexOf('\n') != -1) {
            addBlock(sb,s,level);
            return;
        }
        
        int q2 = s.indexOf('"');
        int q1 = s.indexOf('\'');
        
        if (q1 != -1 && q2 != -1) {
            addBlock(sb,s,level);
            return;
        }
        
        while (n-- != 0)
            sb.append(' ');
        
        if (s.charAt(0) == '#')	// A comment
        	sb.append(s);
        else if ( q1 != -1)
            sb.append('"').append(s).append('"');
        else if (q2 != -1)
            sb.append('\'').append(s).append('\'');
        else if (s.indexOf(' ') != -1) {
            if (q1==-1) {
                sb.append('\'');
                sb.append(s);
                sb.append('\'');
            } else {
                sb.append('"');
                sb.append(s);
                sb.append('"');
            }
        } else if ( s.indexOf('(') != -1 || s.indexOf(')') != -1 || s.indexOf(',') != -1) 
        	sb.append('\'').append(s).append('\'');
        else
        {
            sb.append(s);
        }
        sb.append('\n');
    }
    
    private static void addBlock(StringBuffer sb, String s, int level) 
    {
        int i;
        int n = level*2;

        if (sb.length()<1) {
        	sb.append(s); // XXX
        	return;
        }
        
        // sb.setLength(sb.length()-1);

		for (int j=0; j<n; j++)
			sb.append(' ');
        sb.append("\\\n");
        
        for (int j=0; j<=n; j++)
            sb.append(' ');
        
        for (i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            sb.append(c);
            if (c == '\n') {
                for (int j=0; j<=n; j++)
                    sb.append(' ');
            }
        }
        sb.append('\n');
    }
    
}
