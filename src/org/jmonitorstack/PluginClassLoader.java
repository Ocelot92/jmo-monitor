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
	public Class<?> loadClass (String classname, boolean resolve) throws ClassNotFoundException {
		Class<?> c = null;
		try {
			//check if the class is already loaded
			c = findLoadedClass(classname);

			//... otherwise check if it's a system class
			if (c == null) {
				try{
					System.out.println("i'm above systemclass");
					c = findSystemClass(classname);
				}catch (Exception ex){}
				if ( c == null) {
					//... otherwise load it from the plugin directory
					
					String aux = classname;
					classname += ".class";

					File f = new File (directory, classname);
					// Get the length of the class file, allocate an array of bytes for
					// it, and read it in all at once.
					int length = (int)f.length();
					byte classbytes[] = new byte [length];
					try (DataInputStream dis = new DataInputStream (new FileInputStream(f)) ) {
						dis.readFully(classbytes);
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println(classname + " length: " + length + " classbytes[1] " + classbytes[1] );
					c = defineClass(aux, classbytes, 0, length);
				}
				
				if (resolve)resolveClass(c);
				System.out.println("classe " + classname + "caricata");
				return c;
			}
		} catch (Exception ex) {throw new ClassNotFoundException(ex.toString()); }
		System.out.println("classe non caricata");
		return c;
	}
}

