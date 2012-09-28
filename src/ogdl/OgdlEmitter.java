/* OGDL, Ordered Graph Data Language 
 * (c) R.Veen, 2002-2009.
 * License: zlib (see http://ogdl.org/license.htm)
 */

package ogdl;
import ogdl.IGraph;

/** Graph utilities
 */

public final class OgdlEmitter 
{  
    public static String toString(IGraph g) 
    {
        StringBuffer sb = new StringBuffer();
        
        if (g.getName() != null && !g.getName().equals(Graph._NULL)) 
        {
            addString(sb,g.getName(),0);
            addNodes(sb,g,1);
        }
        else
        	addNodes(sb,g,0);

        return sb.toString();
    }
    
    public static String toString(IGraph g, boolean root) 
    {
        StringBuffer sb = new StringBuffer();
        
        if (root && g.getName() != null && !g.getName().equals(Graph._NULL)) 
        {
            addString(sb,g.getName(),0);
            addNodes(sb,g,1);
        }
        else
        	addNodes(sb,g,0);

        return sb.toString();
    }
    
    /* ---- private section ---- */
    
    private static void addNodes(StringBuffer sb, IGraph g, int level) {
        for (int i=0; i<g.size(); i++) {
            IGraph node = g.get(i);

            addString(sb,node.getName(),level);
                       
            addObject(sb,node.getValue(),level+1);
            addNodes(sb,node,level+1);
        }
    }
    
    private static void addObject(StringBuffer sb, Object o, int level) {
        if (o == null) return;
        addString(sb,o.toString(), level);
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
        
        if ( q1 != -1)
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
    
    /*
    private static void addBlockQuoted(StringBuffer sb, String s, int level) 
    {
        int i;
        int n = level*2;
        
        sb.append('\"');
        
        
        for (i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            sb.append(c);
            if (c == '\n') {
                for (int j=0; j<=n; j++)
                    sb.append(' ');
            }
        }
        sb.append("\"\n");
    } */
    
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
