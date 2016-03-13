package org.jmo;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class PluginClassLoader extends ClassLoader {
	
	File directory;
	
	/* -- Constructors -- */
	/**
	 * Creates a PluginClassLoader. It's used for loading the plugins at runtime.
	 * @param dir - the directory where plugin's .class files are stored.
	 */
	public PluginClassLoader (File dir){
		directory = dir;
	}
	
	/* -- General Methods -- */
	/**
	 * Searches in the plugins directory the class names classname.
	 * @param classname - The name of the class to find
	 * @return The Class found or null.
	 */
	@Override
	public Class<?> findClass (String classname){
		Class<?> c = null;

		String filename = classname + ".class";
		File f = new File (directory, filename);
		int length = (int)f.length();
		byte classbytes[] = new byte [length];

		try (DataInputStream dis = new DataInputStream (new FileInputStream(f)) ) {
			dis.readFully(classbytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		c = defineClass(classname, classbytes, 0, length);
		System.out.println("plugin " + classname + " loaded");
		return c;
	}	
}
