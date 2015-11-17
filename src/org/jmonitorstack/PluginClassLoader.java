package org.jmonitorstack;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class PluginClassLoader extends ClassLoader {
	File directory;
	//**************************Constructors*********************************************
	public PluginClassLoader (File dir){
		directory = dir;
	}
	/********************************************************************************************
	 * Just a convenient call to the 2-args loadClass(String, boolean)
	 */
	@Override
	public Class<?> loadClass (String name) throws ClassNotFoundException { 
	      return loadClass(name, true); 
	}
	/********************************************************************************************
	 * Loads the class given in the classname string. First it do some checks:
	 * 1- class already loaded
	 * 2- classname is a system class
	 * 3- load classname from the directory (this.directory)
	 */
	@Override
	public Class<?> loadClass (String classname, boolean resolve){
		Class<?> c = null;

		//check if the class is already loaded
		c = findLoadedClass(classname);
		//... otherwise check if it's a system class
		if (c == null) {
			try {
				c = findSystemClass(classname);
			} catch (ClassNotFoundException e1) {}
			if ( c == null) {
				//... otherwise load it from the plugin directory
				

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
				if (resolve)resolveClass(c);
				System.out.println("plugin " + classname + " loaded");
			}
		}
		return c;
	}	
}
