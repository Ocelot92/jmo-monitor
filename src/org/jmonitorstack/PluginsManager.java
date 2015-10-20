package org.jmonitorstack;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class PluginsManager {
	private String directory;
	private List <IfcPlugin> plugins;
//The outputs of the plugins' scripts are stored in this queue waiting for being "consumed" by the os client
	private BlockingQueue <JmonitorMessage> resultsQueue; 
	private final int QUEUE_CAPACITY = 10;
	private Timer tmr;
	  
	public PluginsManager (String dir){
		directory = dir;
		plugins = new LinkedList<IfcPlugin> ();
		
		loadPlugins();
	}
	
	public void runPlugins () {
		tmr = new Timer();
		resultsQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
		//creates a task for each plugin and launches it
		for (int i = 0; i < plugins.size(); i++) {
			TimerTask task = new PluginScheduler( (IfcPlugin) plugins.get(i) );
			tmr.scheduleAtFixedRate(task, 1000, plugins.get(i).getRate() );
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
						clsLoaded = cl.loadClass(files[i].substring(0,files[i].indexOf('.')));
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					//check if the class implements IfcPlugin
					if (clsLoaded != null && IfcPlugin.class.isAssignableFrom(clsLoaded) ){
						try {
							IfcPlugin plg = (IfcPlugin) clsLoaded.getDeclaredConstructor(resultsQueue.getClass()).newInstance(resultsQueue);
							plg.initPlugin();
							plugins.add(plg);
							System.out.println("Plugin " + plg.getName() + " loaded successfully");
						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}else{
			System.out.println("Wrong plugins path!");
		}
	}
	
	public BlockingQueue<JmonitorMessage> getResultsQueue () {
		return resultsQueue;
	}
	
	public Timer getTmr() {
		return tmr;
	}
	
}
