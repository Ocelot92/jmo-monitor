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
		//listing all dir's files
		if (dir.exists() && dir.isDirectory()) {
			String files []= dir.list();
			for (int i=0; i < files.length; i++){
				//check only for class files
				if (files[i].endsWith(".class")) {
					Class<?> clsLoaded = null;
					try {
						clsLoaded = cl.loadClass(files[i].substring(files[i].indexOf('.')));
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					//check if the class implements IfcPlugin
					if (clsLoaded != null && checkClass(clsLoaded)){
						try {
							IfcPlugin iplg = (IfcPlugin) clsLoaded.newInstance();
							plugins.add(iplg);
						} catch (InstantiationException | IllegalAccessException e) {
							e.printStackTrace();
						}
						
					}
				}
			}
		}
		
		
	}
	
	//return true if the class implements IfcPlugin
	private boolean checkClass (Class <?> c) {
		Class<?> interfaces [] = c.getInterfaces();
		for (int i = 0; i < interfaces.length; i++){
			//check if the class implements IfcPlugin
			if ( checkInterface(interfaces[i]) )
				return true;
		}
		return false;
	}
	
	private boolean checkInterface (Class<?> intrfc) {
		if (intrfc.getName().equals("IfcPlugin"))
			return true;
		else
			return false;
	}
}
