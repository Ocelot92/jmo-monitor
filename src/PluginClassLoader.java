import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class PluginClassLoader extends ClassLoader {
	File directory;
	
	public PluginClassLoader (File dir){
		directory = dir;
	}
	
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
					DataInputStream dis = new DataInputStream (new FileInputStream(f));
					dis.readFully(classbytes);
					dis.close();
					c = defineClass(classname, classbytes, 0, length);
				}
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return c;
	}	
}
