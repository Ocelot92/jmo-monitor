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
				if (files[i].endsWith(".class")) {
					try {
						Class<?> c = cl.loadClass(files[i].substring(0, files[i].indexOf('.')));
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					
				}
			}
		}
		
	}
}
