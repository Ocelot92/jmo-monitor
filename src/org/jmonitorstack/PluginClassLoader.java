package org.jmonitorstack;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class PluginClassLoader extends ClassLoader {
	File directory;
	
	public PluginClassLoader (File dir){
		directory = dir;
	}
	
//Just a convenient call to the 2-args loadClass
	@Override
	public Class<?> loadClass (String name) throws ClassNotFoundException { 
	      return loadClass(name, true); 
	}
	
	@Override
	public Class<?> loadClass (String classname, boolean resolve){
		Class<?> c = null;

		try {
			//check if the class is already loaded
			c = findLoadedClass(classname);
			//... otherwise check if it's a system class
			if (c == null) {
				c = findSystemClass(classname);
				if ( c == null) {
					//... otherwise load it from the plugin directory
					
					classname += ".class";
					File f = new File (directory, classname);
					int length = (int)f.length();
					byte classbytes[] = new byte [length];
					try (DataInputStream dis = new DataInputStream (new FileInputStream(f)) ) {;
					dis.readFully(classbytes);
					} catch (IOException e) {
						e.printStackTrace();
					}
					c = defineClass(classname, classbytes, 0, length);
				}
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (resolve)resolveClass(c);
		return c;
	}	
}