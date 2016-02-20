package org.jmo;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PluginsManager {
	private File directory;
	private List <IfcPlugin> PLUGINS;
	//The outputs of the plugins' scripts are stored in this queue waiting for being "consumed" by the os client
	private final BlockingQueue <JMOMessage> RESULTS_QUEUE; 
	private final int QUEUE_CAPACITY;
	/********************************************************************************************
	 * Creates a PluginsManager pointing at the directory dir.
	 * @param dir - plugins' directory
	 */
	public PluginsManager (File dir){
		directory = dir;
		PLUGINS = new LinkedList<IfcPlugin> ();
		QUEUE_CAPACITY = 10; //capacity of the BlockingQueue
		RESULTS_QUEUE = new LinkedBlockingQueue<JMOMessage>(QUEUE_CAPACITY);
	}
	/********************************************************************************************
	 * Schedules the plugins for execution at their specific rates.
	 * @param schdExecServ - a ScheduledExecutorService
	 */
	public void runPlugins (ScheduledExecutorService schdExecServ) {
		Iterator <IfcPlugin> i = PLUGINS.iterator();
		IfcPlugin aux = null;
		while (i.hasNext()){
			aux=i.next();
			schdExecServ.scheduleAtFixedRate(aux, 0, aux.getRate(), TimeUnit.SECONDS);
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
							IfcPlugin plg = (IfcPlugin) clsLoaded
									.getDeclaredConstructor(BlockingQueue.class, String.class)
									.newInstance(RESULTS_QUEUE, clsLoaded.getName());
							PLUGINS.add(plg);
						} catch (InstantiationException | IllegalAccessException
							| IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}else{
			System.out.println("Wrong plugins path!");
		}
	}
	/********************************************************************************************
	 * Returns the thread-safe queue where plugins send their outputs.
	 * @return the BlockingQueue where plugins puts their JMOMessages.
	 */
	public BlockingQueue<JMOMessage> getResultsQueue () {
		return RESULTS_QUEUE;
	}
	/********************************************************************************************
	 * Returns the list of the plugins.
	 * @return The List of the IfcPlugin objects.
	 */
	public List<IfcPlugin> getPlugins(){
		return PLUGINS;
	}
}
