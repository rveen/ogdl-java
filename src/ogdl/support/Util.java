package ogdl.support;

// PEND: test append with null

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import ogdl.*;

public class Util {

    public static void reverseIntArray(int[] a)
    {
        int l = a.length/2;
        int j = a.length-1;

        for (int i=0; i<l; i++, j--) {
            int tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
        }
    }

    public static int[] resizeIntArray(int[] i, int size)
    {
        int[] j = new int[size];
        if (size > i.length)
            size = i.length;
        System.arraycopy(i,0,j,0,size);
        return j;
    }

    public static String[] resizeStringArray(String[] s, int size)
    {
        String[] t = new String[size];
        if (size > s.length)
            size = s.length;
        System.arraycopy(s,0,t,0,size);
        return t;
    }

    public static long[] resizeLongArray(long[] i, int size)
    {
        long[] j = new long[size];
        if (size > i.length)
            size = i.length;
        System.arraycopy(i,0,j,0,size);
        return j;
    }

    public static double[] resizeDoubleArray(double[] i, int size)
    {
        double[] j = new double[size];
        if (size > i.length)
            size = i.length;
        System.arraycopy(i,0,j,0,size);
        return j;
    }

    /** return the 'and' of a and b */

    public static int[] intersectIntArray(int[] a, int[] b)
    {
        int n=0,i;
        int[] c;

        // select ints that are in a and b

        if (a.length > b.length) {
            c = new int[a.length];
            Arrays.sort(b);
            for (i=0; i<a.length; i++)
                if (Arrays.binarySearch(b,a[i])> -1)
                    c[n++] = a[i];
        }
        else {
            c = new int[b.length];
            Arrays.sort(a);
            for (i=0; i<b.length; i++)
                if (Arrays.binarySearch(a,b[i])> -1)     //Comprobar return de binarySearch
                    c[n++] = b[i];
        }

        int d[] = new int[n];
        System.arraycopy(c,0,d,0,n);
        return d;
    }

    public static int[] appendIntArray(int[] a, int[] b)
    {
        if (a == null) a = new int[0];
        if (b == null) b = new int[0];
        int[] c = new int[a.length + b.length];
        System.arraycopy(a,0,c,0,a.length);
        System.arraycopy(b,0,c,a.length,b.length);
        return c;
    }

    public static int[] appendIntArray(int[] a, int b)
    {
        if (a == null) a = new int[0];
        int[] c = new int[a.length + 1];
        System.arraycopy(a,0,c,0,a.length);
        c[a.length] = b;
        return c;
    }

    /** remove a value from the int array, if present */
    public static int[] removeIntArray(int[] a, int b)
    {
        int[] c = new int[a.length];
        int i,j;
        for (i=0,j=0; i<a.length; i++)
            if (a[i] != b) c[j++] = a[i];
        return resizeIntArray(c,j);
    }
    
    public static int readShort( byte[] b, int start )
    {
    	int l=0;
    	
    	for (int i=0; i<2; i++) {
            l <<= 8;
    		l |= b[start+i]&0xff;		
    	}
    	
    	return l;
    }

    public static int readInt( byte[] b, int start )
    {
    	int l=0;
    	
    	for (int i=0; i<4; i++) {
            l <<= 8;
    		l |= b[start+i]&0xff;		
    	}
    	
    	return l;
    }
    
    public static long readLong( byte[] b, int start )
    {
    	long l=0;
    	
    	for (int i=0; i<8; i++) {
            l <<= 8;
    		l |= b[start+i]&0xff;		
    	}
    	
    	return l;
    }

    public static void writeShort( byte[] b, int start, int value )
    {

        b[start]   = (byte) ( value >>> 8 );
        b[start+1] = (byte) ( value );
    }
    
    public static void writeInt( byte[] b, int start, int value )
    {

        b[start]   = (byte) ( value >>> 24 );
        b[start+1] = (byte) ( value >>> 16 );
        b[start+2] = (byte) ( value >>>  8 );
        b[start+3] = (byte) ( value        );
    }
    
    public static void writeLong( byte[] b, int start, long value )
    {
        b[start  ] = (byte) (( value >>> 56 ) & 255);
        b[start+1] = (byte) (( value >>> 48 ) & 255);
        b[start+2] = (byte) (( value >>> 40 ) & 255);
        b[start+3] = (byte) (( value >>> 32 ) & 255);
        b[start+4] = (byte) (( value >>> 24 ) & 255);
        b[start+5] = (byte) (( value >>> 16 ) & 255);
        b[start+6] = (byte) (( value >>>  8 ) & 255);
        b[start+7] = (byte) (( value        ) & 255);
    }

    public static String readFile(String name) throws IOException
    {
        StringBuilder sb = new StringBuilder(); 
        InputStreamReader f = new InputStreamReader( new FileInputStream(name), "UTF-8");
        
        int c;
        
        while ( (c=f.read()) != -1) 
            sb.append((char)c);

        f.close();
        return sb.toString();
    }
    
    public static String readFile(File file) throws IOException
    {
    	StringBuilder sb = new StringBuilder(); 
        InputStreamReader f = new InputStreamReader( new FileInputStream(file), "UTF-8");
        
        int c;
        
        while ( (c=f.read()) != -1) 
            sb.append((char)c);

        f.close();
        return sb.toString();
    }    

    public static byte[] readBinaryFile(String file) throws IOException
    {
        File fi = new File(file);
        long i = fi.length();
        
        // XXX i is long, but the next functions require int!
        
        byte[] b = new byte[(int)i];
        
        FileInputStream f = new FileInputStream(fi);
        f.read(b,0,(int)i);
        
        f.close();
        return b;
    }      
    
    public static String unquote(String s) 
    {
    	if (s == null || s.length()<2)
    		return s;
    	
    	int len = s.length();
    	if (s.charAt(0) == '\'' && s.charAt(len-1) == '\'')
    		return s.substring(1,len-1);
    	
    	if (s.charAt(0) == '"' && s.charAt(len-1) == '"')
    		return s.substring(1,len-1);
    	
    	return s;
    }
    
    /** Substitute nodes equal to 'a' by 'b' */
    
    public static void substitute(IGraph g, String a, String b)
    {
    	if (g.getName().equals(a))
    		g.setName(b);
    	
        for (int i=0; i<g.size(); i++) {
        	substitute(g.get(i),a,b);
        }
    }
}

