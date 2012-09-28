package ogdl.template;

import ogdl.*;
import ogdl.support.Util;
import ogdl.template.*;

import java.io.*;

public class ProcessTemplate {
	/**
	 * Takes a hierarchy of source templates and produces a static web from
	 * them.
	 * 
	 * java -cp ogdl.jar:ogdlx.jar ProcessTemplates <tpldir> <outdir>
	 */

	static IGraph context;

	public static void main(String[] args) throws Exception 
	{
		File template = new File(args[0]);

		context = Ogdl.parse("conf.g");
		// System.out.println(context);

		processFile(template);
	}

	public static void processFile(File f) throws Exception 
	{

		String fs = Util.readFile(f);
		Template t = new Template(fs);

		t.print(context);
	}
}
