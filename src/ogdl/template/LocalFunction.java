/* OGDL, Ordered Graph Data Language 
 * (c) R.Veen, 2005-2010.
 * License: zlib (see http://ogdl.org/license.htm)
 */

package ogdl.template;

import ogdl.*;
import ogdl.support.IFunction;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Instrospection front-end that converts Java classes into IFunction's.
 * 
 * $Id$
 * 
 * XXX Spec: from String to IGraph to Java method/field call. This is what ComplexPath should implement
 * XXX Look at org.apache.velocity.util.introspection before redesigning this class.
 */

public class LocalFunction implements IFunction 
{
	HashMap<String, Field> fields;
	Method[] methods;
	Class[] argClass, aC;
	Object[] argObject, aO;
	int nargs;
	Class c = null;
	Object o = null;

	public LocalFunction(String className)
	{
		this(className, null);
	}

	public LocalFunction(String className, IGraph cfg) 
	{
		try {
			c = Class.forName(className);
			o = newInstance(c, cfg);

			methods = c.getMethods();
			getFields();

			argClass = new Class[10];
			argObject = new Object[10];
		} 
		catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
	}

    public Object exec(IGraph g) throws Exception 
	{
    	// System.out.println("LocalFunction.exec(path): \n"+g);

		String token = g.getName();

		/* fields */

		if (g.size() == 0 && fields != null) {
			Field f = (Field) fields.get(token);
			if (f != null)
				return f.get(o);
			// add getter
		}

		getArguments(g);

		try {
			Method m = getMethod(token, aC);
			return m.invoke(o, aO);
		}

		catch (NoSuchMethodException e) {
			e.printStackTrace();
			return message(token);
		}
	}

	private Method getMethod(String name, Class[] args) throws Exception 
	{
		Method m = c.getMethod(name, args);
		if (m != null)
			return m;

		/*
		 * no method with exact the same arguments Get the first less specific
		 * method: XXX search for the best !!
		 */

		for (int i = 0; i < methods.length; i++) {
//System.out.println("LocalFunction: method "+methods[i].getName());
			if (!name.equals(methods[i].getName()))
				continue;
			Class ca[] = methods[i].getParameterTypes();
			if (ca.length != args.length)
				continue;
			int j;
			for (j = 0; j < ca.length; j++) {
				if (!args[j].isAssignableFrom(ca[j]))
					break;
			}
			if (j != ca.length)
				continue;
			return methods[i];
		}
		return null;
	}

	

	/** get all public fields */

	private void getFields() throws Exception {
		Field[] f = c.getFields();
		if (f == null)
			return;

		fields = new HashMap();

		for (int i = 0; i < f.length; i++)
			fields.put(f[i].getName(), f[i]);
	}

	private Object newInstance(Class c, IGraph cfg) throws Exception 
	{
		if (cfg == null) 
		{
			try {
				Object o = c.newInstance();
				return o;
			} catch (Exception ex) {
				return null;
			}
		}
		
		try {
			Constructor co = c.getConstructor(new Class[] { IGraph.class });
			return co.newInstance(new Object[] { cfg });
		} catch (NoSuchMethodException e) {
			try {
				Object o = c.newInstance();
				return o;
			} catch (Exception ex) {
				return null;
			}
		}
	}

	private void getArguments(IGraph g) throws Exception {
		Object o;
		nargs = 0;

//System.out.println("LocalFunction: getArgs:\n"+g);
		
		for (int i = 0; i < g.size(); i++) 
		{
			o = Evaluate.toScalar(g.get(i));

			if (o instanceof Double)
				argClass[i] = double.class;
			else if (o instanceof Long)
				argClass[i] = long.class;
			else if (o instanceof Boolean)
				argClass[i] = boolean.class;
			else if (o instanceof IGraph)
				argClass[i] = ogdl.IGraph.class;
			else
				argClass[i] = o.getClass();
			
			argObject[i] = o;
			nargs++;
		}

		aC = new Class[nargs];
		aO = new Object[nargs];
		for (int i = 0; i < nargs; i++) {
			aC[i] = argClass[i];
			aO[i] = argObject[i];
		}
	}

	private String message(String token) 
	{
		StringBuffer sb = new StringBuffer();
		sb.append("No method ");
		sb.append(token);
		sb.append('(');

		for (int i = 0; i < nargs; i++) {
			if (i > 0)
				sb.append(',');
			sb.append(argClass[i].getName());
		}

		sb.append(") found in class ");
		sb.append(c.getName());

		return sb.toString();
	}

	public void close() {}

	protected void finalize() throws Throwable {
		close();
	}

}
