package ogdl.template;
import ogdl.*;

import java.io.*;

import ogdl.support.Util;
import ogdl.template.*;

public class ProcessTemplates 
{
	/** Takes a hierarchy of source templates and
	 *  produces a static web from them.
	 *  
	 *  java -cp ogdl.jar:ogdlx.jar ProcessTemplates <tpldir> <outdir>
	 */
	
	static IGraph context;
	
	public static void main(String[] args) throws Exception
	{
        File indir = new File(args[0]);
        File outdir = new File(args[1]);
        
        context = Ogdl.parse(args[0]+"/conf.g");
        System.out.println(context);
        
        processDir(indir,outdir);
	}

	public static void processDir(File indir, File outdir) throws Exception
	{
		outdir.mkdir();
		
System.out.println("processDir: "+indir.getName()+", "+outdir.getPath());	

		File[] list = indir.listFiles();
		if (list == null) return;
		
		for (int i=0; i<list.length; i++) 
		{
			File f = list[i];
			String name = f.getName();
			File out = new File(outdir.getAbsolutePath() + "/" + name);
			
			System.out.println(" - "+f.getAbsolutePath()+", "+out.getAbsolutePath());
			
			if ( f.isDirectory()) {				
				processDir(f,out);
			}			
			else if (name.endsWith(".htm")) 
			{
			    FileWriter w = new FileWriter(out);
			    
			    StringWriter sout = new StringWriter();
				
				String fs = Util.readFile(f);
				Template t = new Template(fs);
							
				t.print(sout, context);

                w.write(sout.toString());
			    w.close();
			}
		}
	}
}
