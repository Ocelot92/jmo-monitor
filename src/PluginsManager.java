import java.io.File;
import java.util.List;

public class PluginsManager {
	private String directory;
	private List <IfcPlugin> plugins;
	
	public PluginsManager (String dir){
		directory = dir;
	}
	
	public void loadPlugins (){
		File dir = new File (System.getProperty("user.dir") + File.separator + directory);
		ClassLoader cl = new PluginClassLoader(dir);
		
		if (dir.exists() && dir.isDirectory()) {
			String files []= dir.list();
			for (int i=0; i < files.length; i++){
				//check only for class files
				if (files[i].endsWith(".class")) {
					try {
						Class<?> clsLoaded = cl.loadClass(files[i].substring(files[i].indexOf('.')));
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					//check if the class implements IfcPlugin
					clsLoaded.
				}
			}
		}
		
	}
}
