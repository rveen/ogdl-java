package ogdl.template;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.Enumeration;

import ogdl.*;
import ogdl.support.*;


public class TemplateServlet extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	ServletContext scontext=null;
	String home=null;
	IGraph cfg=null;
    
	public void init(ServletConfig config) throws ServletException 
	{
		super.init(config);

		scontext = config.getServletContext();
		home = scontext.getRealPath("/");

		try {
			cfg = Servlets.init(scontext,home);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
	}

	public void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException 
	{
		long t0 = System.currentTimeMillis();
					
		String path = req.getPathInfo(); 
		String tpl = req.getServletPath();

		if (path != null)
			tpl = tpl + path;
		
		/* Set MIME type. It derives from the file extension. 
		 * Defaults to HTML
		 */
		
		String mime = scontext.getMimeType(tpl);
	
		if (mime == null)
		    mime = "text/html";
		
		res.setContentType(mime+"; charset=UTF-8");

		res.setHeader("Pragma", "no-cache");
		res.setDateHeader("Expires", 0);
		res.setHeader("Cache-Control", "no-cache");
		// res.setHeader("Content-Transfer-Encoding","8bit");
		// req.setCharacterEncoding("UTF-8");
		// res.setCharacterEncoding("UTF-8");
		
		try {
			Exception exc = null;

			IGraph tcontext = Servlets.getContext(req, res, cfg);		
			
			/* Print template */

			StringWriter sout = new StringWriter();
			File f = new File(home + tpl);
			
			String fs = Util.readFile(f);
			Template t = new Template(fs);

			try {
				t.print(sout, tcontext);
			} catch (Exception ex) {
				exc = ex;
			}

			// 'redirect' modules
			String s = tcontext.getString("forward");

			if (s != null && !"".equals(s)) {
				req.getRequestDispatcher(s).forward(req, res);
				return;
			}
			
			Writer out = new BufferedWriter(new OutputStreamWriter(res
					.getOutputStream(), "UTF-8"));

			s = tcontext.getString("redirect");
			if (s != null && !s.equals("")) {

				// if negative number, then take as history
				int i = 0;

				try {
					i = Integer.parseInt(s);
				} catch (Exception ex) {
				}

				if (i < 0)
					out.write("<html><body><script>window.history.go(" + i
							+ ")</script></body></html>");
				else {
					res.sendRedirect(s);
					return;
				}

			}

			String so = sout.toString();
			
			/** hack: Strip empty lines from HTML and XML 		
			
			
			
			if (mime.equals("text/html") || mime.equals("text/xml")) 
			{
			    StringBuilder sb = new StringBuilder();
			    
				char c, cp=0;
				for (int i=0; i<so.length(); i++)
				{
					c = so.charAt(i);
					if (!(c=='\n' && cp=='\n'))
						sb.append(c);
					cp = c;
				}
				so = sb.toString();
			}
			
			*/
			
			out.write(so);
			
			if (mime.equals("text/html")) {
				if (exc != null)
					out.write("<pre>" + Graphs.get(exc) + "</pre>");

				/*long t1 = System.currentTimeMillis() - t0;
				out.write("<center>Processing time: " + t1 + " ms</center>");*/
			}
			out.close();

		} catch (Exception ex) {
			PrintWriter out = new PrintWriter(new OutputStreamWriter(res
					.getOutputStream()));
			ex.printStackTrace(out);
			out.close();
		}
	}
}
