package ogdl.misc;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class TestUrlEncoding 
{

	public static void main(String[] args) throws UnsupportedEncodingException 
	{
		String s = java.net.URLEncoder.encode("cóncholis","UTF-8");
		s = "file="+s;
		
		System.out.println(s);
		// TODO Auto-generated method stub

		String d = java.net.URLDecoder.decode(s, "UTF-8");
		System.out.println(d);
		
		HashMap h = new HashMap();
		
		parseParameters(h,d,"UTF-8");
		
		System.out.println(h);
	}

	/* org.apache.catalina.util.RequestUtil fragment
	 * 
	 * Tomcat 6.0.20, (c) Apache foundation.
	 *  
	 */
	public static void parseParameters(Map map, String data, String encoding)
    throws UnsupportedEncodingException {

        if ((data != null) && (data.length() > 0)) {

            // use the specified encoding to extract bytes out of the
            // given string so that the encoding is not lost. If an
            // encoding is not specified, let it use platform default
            byte[] bytes = null;
            try {
                if (encoding == null) {
                    bytes = data.getBytes();
                } else {
                    bytes = data.getBytes(encoding);
                }
            } catch (UnsupportedEncodingException uee) {
            }

            parseParameters(map, bytes, encoding);
        }

    }
	
	public static void parseParameters(Map map, byte[] data, String encoding)
    throws UnsupportedEncodingException {

    if (data != null && data.length > 0) {
        int    ix = 0;
        int    ox = 0;
        String key = null;
        String value = null;
        while (ix < data.length) {
            byte c = data[ix++];           
            switch ((char) c) {
            case '&':
                value = new String(data, 0, ox, encoding);
                if (key != null) {
                    putMapEntry(map, key, value);
                    key = null;
                }
                ox = 0;
                break;
            case '=':
System.out.println("ox="+ox);             	
                if (key == null) {
//System.out.println("ox="+ox);                	
                    key = new String(data, 0, ox, encoding);
                    ox = 0;
                } else {
                    data[ox++] = c;
                }                   
                break;  
            case '+':
                data[ox++] = (byte)' ';
                break;
            case '%':
                data[ox++] = (byte)((convertHexDigit(data[ix++]) << 4)
                                + convertHexDigit(data[ix++]));
                break;
            default:
                data[ox++] = c;
            }
        }
System.out.println("key="+key);        
        //The last value does not end in '&'.  So save it now.
        if (key != null) {
            value = new String(data, 0, ox, encoding);
            putMapEntry(map, key, value);
        }
    }
	}

    private static byte convertHexDigit( byte b ) {
        if ((b >= '0') && (b <= '9')) return (byte)(b - '0');
        if ((b >= 'a') && (b <= 'f')) return (byte)(b - 'a' + 10);
        if ((b >= 'A') && (b <= 'F')) return (byte)(b - 'A' + 10);
        return 0;
    }


    /**
     * Put name and value pair in map.  When name already exist, add value
     * to array of values.
     *
     * @param map The map to populate
     * @param name The parameter name
     * @param value The parameter value
     */
    private static void putMapEntry( Map map, String name, String value) {
        String[] newValues = null;
        String[] oldValues = (String[]) map.get(name);
        if (oldValues == null) {
            newValues = new String[1];
            newValues[0] = value;
        } else {
            newValues = new String[oldValues.length + 1];
            System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
            newValues[oldValues.length] = value;
        }
        map.put(name, newValues);
    }
}
