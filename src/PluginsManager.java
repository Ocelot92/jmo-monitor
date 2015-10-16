import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class PluginsManager {
	private String directory;
	private List <IfcPlugin> plugins;
	
	public PluginsManager (String dir){
		directory = dir;
		plugins = new LinkedList<IfcPlugin> ();
	}
	
	public void runPlugins () {
		Timer tmr = new Timer();
		
		//create a task for each plugin and launch it
		for (int i = 0; i < plugins.size(); i++) {
			TimerTask task = new PluginScheduler( (IfcPlugin) plugins.get(i) );
			tmr.scheduleAtFixedRate(task, 1000, plugins.get(i).getRate());
		}
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
							IfcPlugin plg = (IfcPlugin) clsLoaded.newInstance();
							plugins.add(plg);
						} catch (InstantiationException | IllegalAccessException e) {
							e.printStackTrace();
						}
						
					}
				}
			}
		}
		
		
	}
	
	//return true if the class extends IfcPlugin
	private boolean checkClass (Class <?> c) {
		if (IfcPlugin.class.isAssignableFrom(c))
			return true;
		return false;
	}
}
