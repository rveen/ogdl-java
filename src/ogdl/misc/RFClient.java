/* OGDL, Ordered Graph Data Language 
 * (c) R.Veen, 2002-2006.
 * License: see http://ogdl.org/ (similar to zlib)
 */

package ogdl.misc;

import java.net.*;
import java.util.logging.*;
import java.io.*;
import javax.net.*;
import javax.net.ssl.*;

import ogdl.*;
import ogdl.support.EventHandlerGraph;
import ogdl.support.IFunction;
import ogdl.template.*;

/**
 * OGDL/RF Client functions.
 */

public class RFClient implements IFunction
{
	public final static int TIMEOUT_LONG = 20000;
	public final static int TIMEOUT_PING = 5000;
	
    InputStream in=null;
    OutputStream out=null;
    Socket socket= null;
    String host;
    int port;
    int timeout = TIMEOUT_LONG;
    boolean ssl=false;
    int retry = 0;
    static Logger log = Logger.getLogger(RFServer.class.getName());
    
    /** This flag is set when we have to send the complete
     *  graph, including the root node name.
     *  
     *  XXX This should be clarified in some specification!
     *  
     *  Do we want to transport exact graphs? Not allways.
     *  We could a root in case the graph's root is not _root.
     */
    
    boolean root=false;
    
    /** Creates a new instance of RFClient 
     * 
     *  This instance maintains an open connection
     *  with the server end, until it is closed.
     */
   
    public RFClient(String host, int port) throws Exception 
    {
    	log.setLevel(java.util.logging.Level.ALL);
    
    	open(host,port,0);
    }
    
    public RFClient(String host, int port, int timeout) throws Exception 
    {
    	open(host,port,timeout);
    }
    
    public RFClient(String host, int port, boolean secure) throws Exception 
    {
    	if (secure) openssl(host,port,0); else open(host,port,0);
    }
    
    public RFClient(String host, int port, int timeout, boolean secure) throws Exception 
    {
    	if (secure) 
    		openssl(host,port,timeout); 
    	else 
    		open(host,port,timeout);
    }
   
    public RFClient(IGraph conf /*, IGraph context*/) throws Exception 
    {
    	String host = conf.getString("host");
    	String s = conf.getString("port");
      	
    	if (host==null || s==null)
    		return;
    	
    	String r = conf.getString("retry");
    	if (r!=null) 
    		retry = Integer.parseInt(r);
    	
    	int port = Integer.parseInt(s);
        
    	root = true;
    	
    	open(host,port,0);
    }    
    
    public void open(String host, int port, int tout) throws Exception
    {
    	socket = new Socket();
    	socket.setSoTimeout( timeout==0? timeout:tout);
    	socket.setTcpNoDelay(true);
 	
    	socket.connect(new InetSocketAddress(host,port));
    	
    	//socket = new Socket(host,port);
      
        out = socket.getOutputStream();
        in = socket.getInputStream();
        
        this.host = host;
        this.port = port;
    }
    
    public void openssl(String host, int port, int tout) throws Exception
    {
    	SocketFactory socketFactory = SSLSocketFactory.getDefault();
        socket = socketFactory.createSocket(host, port);

        socket.setSoTimeout( timeout==0? timeout:tout);
        socket.setTcpNoDelay(true);
        
        out = socket.getOutputStream();
        in = socket.getInputStream();

        this.host = host;
        this.port = port;
        ssl = true;
    }
    
 
    public void close()
    {   	
        try {
            if (socket != null) 
            	socket.close();  
            socket = null;
        }
        catch (Exception ex) {}
    }
    
    public boolean isConnected()
    {
    	if (socket == null || !socket.isConnected())
    		return false;
    	return true;
    }
    
    public Object exec (IGraph g) throws Exception
    {
    	
    	if (socket == null || !socket.isConnected())
    		throw new Exception ("No open connection to host");
    	
    	int i = retry+1;
    	
    	/* clean pending characters */
    	in.skip(in.available());
    	
    	while (true) 
    	{		
    	    try {
    	    	OgdlBinaryEmitter.write(g,out);
    	    	log.finest("Sent:\n"+g);
    	    }
    	    catch (Exception ex) {
    	    	ex.printStackTrace();    	    	
    	    	i--;
    		    if (i>0) {
    			    close();
        		    if (ssl)
        			    openssl(host,port,0);
        		    else
        		        open(host,port,0);
        		    break;
    		    }
    		    else throw new Exception(ex);
    	    }
    	    break;
    	}
    	
        out.flush(); 
          
        EventHandlerGraph handler = new EventHandlerGraph();         
      	OgdlBinaryParser p = new OgdlBinaryParser(in,handler);
       	boolean ok = p.parse2();
       	if (!ok) {
       		IGraph error = new Graph("error");
       		error.add("parse error (no header)");
       		error.add("sent graph name was "+g.getName());
System.out.println("RFClient: no header received");       		
       		return error;
       	}
       	IGraph r = handler.get(); 

		String name = r.getName();
		if (name.equals(Graph._NULL))
			name = r.getName(0);
		
        if ("error".equals(name))
        	throw new Exception(""+r);
        
        return r;
    }
    
    public IGraph call(IGraph g) throws Exception
    {   	
    	return (IGraph) exec(g);
    } 
    
    public IGraph callRetry(IGraph g) throws Exception
    {   	
    	try {
    		ping();
    	}
    	catch (Exception ex)
    	{
    		close();
    		if (ssl)
    			openssl(host,port,0);
    		else
    		    open(host,port,0);
    	}
    	
        OgdlBinaryEmitter.write(g,out);       
        out.flush();        
        return Ogdl.parseBinary(in);
    } 
    
    public long ping() throws Exception
    {
    	return ping(TIMEOUT_PING);
    }
    public long ping(int millis) throws Exception
    {
    	//if (socket == null || !socket.isConnected())
    		//throw new Exception ("socket is not connected");
 
    	socket.setSoTimeout(millis);
    	socket.setTcpNoDelay(true);
    	
    	IGraph f = new Graph();
    	f.add("version");
    	
    	long t0 = System.currentTimeMillis();
    	
    	IGraph r = call(f);	
    	
//System.out.println("r--:\n"+r);    	
    	long t1 = System.currentTimeMillis();
    	
    	socket.setSoTimeout(TIMEOUT_LONG);
    	if (r == null || r.size()==0 || r.get(0).getName().length()==0 )
    		throw new Exception("timeout or incorrect response from host");
    	
    	return t1 - t0;
    }
    
    public long checkConnection() throws Exception
    {
    	return ping();
    }
    
    public long checkConnection(int timeout) throws Exception
    {
    	return ping(timeout);
    }
    
    public void setTimeout(int millis) throws Exception
    {
    	socket.setSoTimeout(millis);	
    }
	
    /** Static call, opens and closes the connection.
     */
      
    public static IGraph function(String host, int port, IGraph g) throws Exception
    {
        Socket client = new Socket(host,port);
        client.setSoTimeout(30000);

        OutputStream out = client.getOutputStream();
        InputStream in = client.getInputStream();      

        OgdlBinaryEmitter.write(g,out);
        out.flush(); 
        IGraph q = Ogdl.parseBinary(in);
    
        client.close();
        
        return q;        
    }
    
    public static IGraph function(String host, int port, String path) throws Exception
    {
    	Graph g = new Graph();
    	g.add("f").add(path);

    	return function(host,port,g);
    }
    
}
