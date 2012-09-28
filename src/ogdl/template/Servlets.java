package ogdl.template;

import javax.servlet.*;
import javax.servlet.http.*;
import ogdl.*;
import ogdl.support.QueryStringParser;
import ogdl.support.Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.List;
import java.util.Enumeration;
import java.util.Vector;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.fileupload.disk.*;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

import java.util.TreeMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;

public class Servlets 
{
	final static String CONF_DIR = "WEB-INF";
	final static String CONF_FILE = "conf.g";
	final static String LOGOUT_URL = "/logout.htm";
	final static String RELOAD_URL = "/reload.htm";
	
	/** Singleton: holds the application conf.g */
	
	static IGraph cfg=null;
	// static Log log;
	static Random random;
	
	static String confFile;
	
	public static IGraph init(ServletContext ctx, String home) throws Exception
	{
		// log = Log.getLog(Servlets.class);
		random = new Random();
		
		confFile = home + CONF_DIR + "/" + CONF_FILE;
		
		
		ctx.setAttribute("confDir", home + CONF_DIR + "/");
		
		try {
			loadConf(ctx, confFile);
		} catch (ServletException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
		return cfg;
	}
	
	public static void loadConf(ServletContext ctx, String file) throws Exception
	{	
		cfg = Ogdl.parse(file);
		cfg.setName("conf");
		ctx.setAttribute("conf", cfg);
		
	}

	public static Graph getContext(HttpServletRequest req,
			HttpServletResponse res, IGraph conf) 
	{
		Graph c, r;
		boolean reload=false;

		HttpSession session = req.getSession();

		String uri = req.getRequestURI();
		if (uri.endsWith(LOGOUT_URL)) {
			session.invalidate();
			session = req.getSession();
		}
		else if (uri.endsWith(RELOAD_URL))
		{
			reload = true;
		}
		
		ServletContext context = session.getServletContext();
		String confDir = (String) context.getAttribute("confDir");
		
		if (reload) {
			try {
				loadConf(context,confDir);
				//log.info("Reloading conf.g from "+confDir);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				//log.info("Couldn't reload conf.g");
			}
			
		}

		/*
		 * if it is a new session, add default user and clone config. If the
		 * session has attribute _S, then it is not new.
		 */

		r = new Graph("this");
		r.add( new Graph("_R", req) );
		r.add( new Graph("_Rs",res ) );

		if ((c = (Graph) session.getAttribute("_C")) == null || reload) {
			
            // log.info("no session: creating copy of C");
			
			// XXX Load the file directly ?
			
			c = (Graph) cfg.clone();
			c.setName("C");
			session.setAttribute("_C", c);
		} 

		/* Jetty and Apache don't return anything with getRemoteUser */

		String u = req.getRemoteUser();
		if (u == null) {
			/* try authentication header */
			u = req.getHeader("authorization");
			if (u != null) {
			    u = Base64.decodeString(u.substring(6));
			    int i = u.indexOf(':');
			    if (i!=-1)
			    	u = u.substring(0,i);
			}
		}

		// log.debug("remoteUser: "+u);
		
		if (u == null)
			u = "nobody";

		session.setAttribute("user", u);
		
		try {
			c.set("user",u);
		} catch (Exception e1) {}

		IGraph p = c.getNode("profile");
		if (p == null) 
		{
			try {
				/* load the user's profile the first time */
				IGraph userProfile = Ogdl.parse(confDir + "/users/" + u + ".g");
				if (userProfile != null) {
				    userProfile.setName("profile");
				    c.set("profile", userProfile);
				}
			} 
			catch (Exception ex) {
				ex.printStackTrace();
			}
			
			Util.substitute(c,"$user",u);
		}

		/* add GET / FORM POST data */
		try {
			IGraph re = r.add("R");
			IGraph data = getFields(req);
			re.add(data); 
			re.add(getHeaders(req));
			re.add(getFiles(req,data));
			reduceIndexes(data);
			re.add("id").add(""+ Math.abs(random.nextInt()) );
			
			r.add("Q").addNodes(data); // $Q = $R.data
		} catch (Exception e) {
		}

		r.add(c);
		r.add( new Graph ("_S", session) );

		/*
		 * This is for $math instead of $C.math, for example
		 * 
		 * XXX should be extended to allow several graphs.
		 */

		try {
			IGraph gg = (IGraph) c.get("_promote");
			if (gg != null)
				r.addNodes(gg);
		} catch (Exception e) {
		}

		/* add some useful variables */
		
		r.add("home").add(context.getRealPath("/"));
		r.add("app").add(req.getContextPath());
		
		return r;
	}

	/**
	 * Read GET and POST data from the incoming HTTP request and put that in a
	 * Graph.
	 * 
	 * Important: only name=value pairs are taken into account.
	 * 
	 * The parameter name can be preceeded by a type specifier (type:name). Some
	 * types are transformed into their corresponding classes ('list','oid'). In
	 * any case, the type specifier is copied into the node type attribute.
	 * 
	 * Object trees can be specified by using paths instead of simple names
	 * (example: "d.name.a").
	 * 
	 * Objects are ordered if they have an integer attribute '__order' .
	 * 
	 * [!] The form data is decoded with URLDecoder, assuming it comes in UTF-8
	 */

	public static IGraph getFields(HttpServletRequest req) throws Exception
	{
		IGraph g = new Graph("data");
		
		/* before reading params, set the encoding! */	
		req.setCharacterEncoding("UTF-8");
		
		boolean post = "POST".equals(req.getMethod());
		
System.out.println("Servlets.methodIsPost:"+post);
System.out.println("Servlets.charEnc="+req.getCharacterEncoding());
System.out.println("Servlets.queryString="+req.getQueryString());
	
		HashMap<String, String[]> pars = new HashMap<String, String[]>();
		
		if (!post) 
		{
			/* Reparsing the query string because Tomcat (or some installed filter) fails:
			 * it parses the GET parameters too early and with the wrong enconding.
			 * 
			 * XXX bullshit.
			 * 
			 * Parameters found here have precedence
			 */
			
			String q = req.getQueryString();
			if (q!=null && q.length()!=0) {
    		    // String t = java.net.URLDecoder.decode(q,"UTF-8");	
    		    QueryStringParser.parseParameters(pars, q, "UTF-8");
			}   		
		}
		
		Enumeration param = req.getParameterNames();
		while (param.hasMoreElements()) 
		{
			String name = (String) param.nextElement();
			String value[] = req.getParameterValues(name);
			if (value == null)
				continue;

			if (!pars.containsKey(name)) // queryString has precedence
			    pars.put(name, value);
		}
		
		Set<String> e = pars.keySet();
		Iterator<String> it = e.iterator();

		while (it.hasNext()) {
			String name = it.next();
			String[] value = pars.get(name);

			if (value == null)
				continue;

			try {
				for (int i = 0; i < value.length; i++)
					processTypes(name, value[i], g);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		
		return order(g);
	}
	
	static SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static SimpleDateFormat sf2 = new SimpleDateFormat("yyyy-MM-dd");
	
	// XXX process errors (specially dates)
	
	static void processTypes(String name, String value, IGraph g) throws Exception
	{
		String vname = inputParseName(name);
	    String type = inputParseType(name);
	    
	    if ("ogdl".equals(type)) 
			g.add(vname,Ogdl.parseString(value));
		
	    else if ("datetime".equals(type)) {
	    	// Convert YYYY-MM-DD HH:MM:SS to time
	    	if (value.length() == 10)
	    	{
	    		Date da = sf2.parse(value);		
		        g.add(vname,new Graph(""+ da.getTime()));
	    	}
	    	else {
	    	    Date da = sf.parse(value);		
		        g.add(vname,new Graph(""+ da.getTime()));
	    	}
	    }
	    /*
	    else if (value!=null && value.length()!=0)
		    g.add(vname,new Graph(value)); 
	    else
	    	g.add(vname, null);
	    	*/
	    else
	    	g.add(vname,value);
	}
	
	/** RFC1867 file attachments (multipart/form-data handling, also fields)
	 * 
	 * Using Apache Commons FileUpload for RFC1867 handling
	 */
	
	public static IGraph getFiles(HttpServletRequest req, IGraph data) throws Exception
	{
		IGraph g = new Graph("files");
		
		if (ServletFileUpload.isMultipartContent(req)) 
		{
			DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setSizeThreshold( 100000 );
			factory.setRepository( new File("/tmp") );
			
			ServletFileUpload upload = new ServletFileUpload(factory);
			
			upload.setSizeMax( 100000000 );
			
			List items = upload.parseRequest(req);	
			
			for (int i=0; i<items.size(); i++)  {
				DiskFileItem f = (DiskFileItem) items.get(i);
				
				if (!f.isFormField()) {
					IGraph fi = g.add(f.getFieldName());
					fi.add("name").add(f.getName());
					fi.add("type").add(f.getContentType());
					
					if (f.isInMemory()) {
						if ( f.getContentType().startsWith("text/")) 
						    fi.add("_content").add(f.getString("UTF-8"));
						else
						    fi.add( new Graph ("_binary",f.get()) );
					}
					fi.add( new Graph("_object",f ));
				}
				else {
					String name = f.getFieldName();
				    String value = f.getString("UTF-8");
				    
				    processTypes(name,value,data);
				}
			}
		}
		return g;
	}
	
	public static IGraph getHeaders(HttpServletRequest req) throws Exception
	{
		IGraph g = new Graph("header");

		Enumeration param = req.getHeaderNames();
		
		while (param.hasMoreElements()) {
			String name = (String) param.nextElement();
			String value = req.getHeader(name);
			g.add(name).add(value);
		}
		return g;
	}
	
	/* type:path */

	private static String inputParseType(String s) {
		int index = s.indexOf(":");

		if (index == -1)
			return null;
		return s.substring(index + 1);
	}

	private static String inputParseName(String s) {
		int index = s.indexOf(":");

		if (index == -1)
			return s;
		return s.substring(0, index);
	}

	/**
	 * arrange the graph nodes in ascending __order. Used to add order to HTML
	 * FORM objects.
	 * 
	 * @deprecated
	 */

	public static IGraph order(IGraph g) throws Exception {
		Vector v = new Vector();

		for (int i = 0; i < g.size(); i++) {
			IGraph obj = g.get(i);

			int order;
			try {
				String s = obj.getString("__order");
				if (s!=null)
				    order = Integer.parseInt(s);
				else
					order = 9999;
			} catch (Exception e) {
				order = 9999;
			}

			if (order == 9999)
				v.add(obj);
			else {
				// search insertion point
				for (int j = 0; j < v.size(); j++) {
					Graph node = (Graph) v.get(j);
					try {
						String s = obj.getString("__order");
						int ord;
						
						if (s!=null)
						    ord = Integer.parseInt(s);
						else
							ord = 9999;
						
						if (ord > order) {
							v.add(j, obj);
							break;
						}
					} catch (Exception e) {
						v.add(obj);
						break;
					}
				}
			}
		}

		IGraph g2 = g.copy();

		for (int i = 0; i < v.size(); i++)
			g2.add((Graph) v.get(i));

		return g2;
	}
	
	/** remove !i 
	 * 
	 * Doesn't insert missing indexes!
	 * 
	 * @deprecated XXX
	 * */
	
	static void reduceIndexes (IGraph g)
	{
		IGraph nul = new Graph("-");
		
		if ("!i".equals(g.getName((0)))) {
			
			TreeMap map = new TreeMap();
			
			IGraph ix = g.get(0);
			for (int i=0; i<ix.size(); i++) {
				IGraph node = ix.get(i);
				if (node.size() == 0)
				    map.put(node.getName(),nul);
				else
					map.put(node.getName(),node.get(0));
			}
			g.remove();
			
			Set set = map.entrySet();
			
			Iterator it = set.iterator();
			
			while (it.hasNext()) {
				Map.Entry me = (Map.Entry) it.next();
				g.add((IGraph)me.getValue());
			}
		}
		else
		for (int i=0; i<g.size(); i++)
			reduceIndexes(g.get(i));
	}
}
