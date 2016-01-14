package org.jmo;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PluginsManager {
	private File directory;
	private List <IfcPlugin> plugins;
	//The outputs of the plugins' scripts are stored in this queue waiting for being "consumed" by the os client
	private BlockingQueue <JMOMessage> resultsQueue; 
	private final int QUEUE_CAPACITY = 10;
	private ScheduledExecutorService schedThreadPool;
	//****************************Constructors****************************************************
	public PluginsManager (File dir, ScheduledExecutorService ses){
		directory = dir;
		plugins = new LinkedList<IfcPlugin> ();
		resultsQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
		schedThreadPool=ses;
	}
	/********************************************************************************************
	 *Runs the plugins in the plugins List by scheduling them at their specific rates.
	 */
	public void runPlugins () {
		//creates a task for each plugin and launches it
		for (int i = 0; i < plugins.size(); i++) {
			schedThreadPool.scheduleAtFixedRate(plugins.get(i), 0, plugins.get(i).getRate(), TimeUnit.SECONDS);
		}
	}

	/********************************************************************************************
	 *This method uses the PluginClassLoader to load all the class files - specified in the directory
	 * field - which extends the IfcPlugin abstract class. Once loaded the plugins are added in the plugins List.
	 */
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
							IfcPlugin plg = (IfcPlugin) clsLoaded.getDeclaredConstructor(BlockingQueue.class).newInstance(resultsQueue);
							plg.initPlugin();
							plugins.add(plg);
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
	//********************Accessor Methods********************************************************
	public BlockingQueue<JMOMessage> getResultsQueue () {
		return resultsQueue;
	}
	public List<IfcPlugin> getPlugins(){
		return plugins;
	}
}
