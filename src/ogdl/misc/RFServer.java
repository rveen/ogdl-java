/* RFunction.java
 * (c) Rolf Veen, 2006-2009.
 * This file is part of the OGDL project.
 * License: see ogdl.org.
 * 
 * Multi-threaded version.
 */

package ogdl.misc;

import java.io.*;
import java.net.*;

import ogdl.*;
import ogdl.support.IFunction;
import ogdl.template.*;
import javax.net.*;
import javax.net.ssl.*;

import java.util.HashMap;
import java.util.logging.*;
import java.util.Vector;

public class RFServer implements Runnable
{
	final Vector  handlers = new Vector();
    final boolean useNagle = false; 
    final Thread  acceptThread;
    int port;
    ServerSocket  ssocket;
    volatile boolean aborting = false;
    HashMap<String, IFunction> h = new HashMap<String, IFunction>();
    
    boolean ssl=false;
    
	IGraph g_error, g_exception, g_version, g_name;
	IGraph cla;

	Logger log = Logger.getLogger(RFServer.class.getName());
	
	public RFServer(int port, IGraph cfg, boolean ssl) throws Exception
    {
        this.port = port;
        ssocket = null;
        this.ssl = ssl;

		cla = cfg.getNode("classes");
		if (cla == null) 
		    throw new Exception("Configuration doesn't contain defined classes");
		
		String name = cfg.getString("name");
		if (name == null)
			name = "RFServer";
		g_name = new Graph(name);
		
		/** default error response */
		g_error = new Graph();
		g_error.add("error").add("unsupported function");
		g_version = new Graph();
		g_version.add("version").add("0.2");
        
        
        acceptThread = new Thread(this);
        acceptThread.start();
    }
	
	public void run()
    {
        try
        {
        	if (!ssl) {
    		    ssocket = new ServerSocket(port);
    		}
    		else {
    			ServerSocketFactory ssocketFactory = SSLServerSocketFactory.getDefault();
                ssocket = ssocketFactory.createServerSocket(port);
    		}
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return;
        }

        while (!aborting)
        {
            try
            {
            	Socket socket = ssocket.accept();
            	// socket.setSoTimeout(1000);

    			log.info("Connection accepted");


                if (!useNagle)
                {
                    socket.setTcpNoDelay(true);
                }

                RFHandler handler = new RFHandler(this,socket);
                Thread t = new Thread(handler);
                t.start();
                // handlers.addElement(handler);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

	void close()
    {
        // signal that the server is aborting
        synchronized(this)
        {
            aborting = true;
        }

        // close the server socket
        if (ssocket != null)
        {
            try
            {
                ssocket.close();
                synchronized(this)
                {
                    ssocket = null;
                }
            }
            catch (IOException e)
            {
            }
        }

        // close the handlers
        for (int i = 0; i < handlers.size(); i++)
        {
            RFHandler handler =
                (RFHandler) handlers.elementAt(i);
            handler.close();
        }
    }


    void handleClose(RFHandler handler)
    {
        handlers.removeElement(handler);
    }

	
	public static void main(String argv[]) throws Exception 
	{
        boolean ssl=false;
        String file = null;
        IGraph cfg = null;
        boolean daemon = false;

		int port = 1111;
		
		for (int i=0; i<argv.length; i++) {
		    String s = argv[i];
		    if ("-p".equals(s)) {
		    	port = Integer.parseInt(argv[i+1]);
		    	i++;
		    }
		    if ("-f".equals(s)) {
		    	file = argv[i+1];
		    	i++;
		    }
		    else if ("-ssl".equals(s))
		    	ssl = true;
		    else if ("-d".equals(s))
		    	daemon = true;
		}
		
		if (file == null) {
		    System.out.println("Usage: ogdl.misc.RFServer [-ssl] [-p port] -f config.g");
		    return;
		}
		
		cfg = Ogdl.parse(file);
		if (cfg == null) {
		    System.out.println(file + " doesn't contain a valid configuration");
		    return;
		}
		
		new RFServer(port,cfg,ssl); 
		
		if (!ssl) {   
		    System.out.println("[OK] Starting RFServer on port "+port);
		}
		else {
            System.out.println("[OK] Starting RFServer(SSL) on port "+port);
		}
		
		/* Detach from console so that we can go to the background */
		
		if (daemon) {
		  System.out.close();
		  System.err.close();
		}
		
		while (true) { Thread.sleep(5000); }
	}

	IGraph function(String fn, IGraph g) 
	{		
        if ("version".equals(fn)) 
			return g_version;
        if ("name".equals(fn)) 
			return g_name;
        
		return null;
	}

	class RFHandler implements Runnable
	{
		RFServer rf;
		Socket s2;
		
		public RFHandler(RFServer rf, Socket s2) 
		{
			this.rf = rf;
			this.s2 = s2;
		}
		
		public void run()  
		{
			InputStream in;
			OutputStream out;
			IFunction f = null;
			
			try {
			in = s2.getInputStream();
			out = s2.getOutputStream();

			while (true) 
			{
				IGraph g = null;
				try {
				    g = Ogdl.parseBinary(in); // XXX reuse parser object
				}
				catch (java.net.SocketTimeoutException ex) {
					log.warning(ex.getMessage());
				}
				catch (Exception ex) {
					ex.printStackTrace();
					log.severe(ex.getMessage());
					break;
				}
				
				if (g == null) break;

				IGraph xx = g.get(0);
				if (xx == null) break;		// Exit when broken pipe
				
				log.finest("Received object:\n"+g);
				
				IGraph q = null;
				String fn = null;
				try {
					fn = g.getName(0);						
					q = function(fn, g);
					log.finest("function: "+fn);
					
				} catch (Exception ex) {				
					q = new Graph("error");
					q.add(ex.getMessage());
					ex.printStackTrace();
				}
				
				if (q == null) {
					try {
						// Get instance of the class.
						f = (IFunction) rf.h.get(fn);
						if (f == null) 
						{
							/* This breaks if NULL nodes are not filtered out (in OgdlBinaryEmitter) */
							IGraph c = rf.cla.getNode(fn);
							if (c == null)
								log.severe("No class data for "+fn);
							IGraph ty = c.getNode("!type");
							if (ty == null)
								log.severe("No !type for "+fn);
							String ft = c.getNode("!type").getName(0);
							log.finest("function: "+fn+", type: "+ ft);
							f = Evaluate.getFunction(ft, c);
							h.put(fn, f);
						}

						if (f != null) {
							Object o = f.exec(g.get(0).get(0));
							if (o!=null) log.fine("f.exec="+o.getClass().getName());
							if (o instanceof IGraph)
								q = (IGraph) o;
							else if (o==null)
								q = new Graph();
							else
								q = new Graph(""+o);
							log.finest("f.exec(q)="+q.getName());
						}

					} catch (Exception ex) {
						ex.printStackTrace();
						q = new Graph("error");
						q.add(ex.getMessage());
					}
				}

				if (q == null)
					q = g_error;

				log.finest("going to write:\n"+q);
				OgdlBinaryEmitter.write(q, out);

			}

			in.close();
			out.close();
			s2.close();
			log.info(" ... connection released");	
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}

		}
		
		public void close() {}
	}
}
