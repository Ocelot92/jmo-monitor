package org.jmo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.openstack4j.api.OSClient;
import org.openstack4j.openstack.OSFactory;

public class JMOCore {
	private static final JMOCore INSTANCE = null;
	private final String SWIFT_CONTAINER_NAME;
	private final OSClient OS;
	private final PluginsManager PM;
	private final Date NOW;
	private final int LOG_SIZE; //max size of the log in bytes
	private final String LOCAL_DIR;
	private final int READINESS; //rate (in seconds) at which update logs on Swift
	private final ScheduledExecutorService SCHED_EXEC_SERV;
	private final Set<File> PENDING_LOGS; //tracks logs not uploaded to Swift yet
	private final String HOSTNAME;
	/********************************************************************************************
	 * Creates a JMOCore and initializes it by loading the properties in a JMO-config.properties
	 * file. This file must be in the same folder of the Java Application.
	 * It throws FileNotFouundException if can't find the JMO-config.properties file or
	 * IOException if I/O error occurred while reading the properties file or loading it.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private JMOCore() throws FileNotFoundException, IOException{
		LOCAL_DIR = "logs";
		NOW = new Date();
		PENDING_LOGS = (Set<File>) Collections.synchronizedSet(new HashSet<File> ());
		
		InputStream hostStream = Runtime.getRuntime().exec("/bin/hostname").getInputStream();
		try(Scanner scan = new Scanner(hostStream)) {
			scan.useDelimiter("\\A");
			HOSTNAME = scan.hasNext() ? scan.next().trim() : ""; 
		}
		
		Properties prop = new Properties();
		//load parameters from config file
		try(InputStream is = new FileInputStream ("JMO-config.properties") ){
			prop.load(is);

			OS = OSFactory.builder()
					.endpoint(prop.getProperty("URLendpoint"))
					.credentials(prop.getProperty("user"),prop.getProperty("password"))
					.tenantName(prop.getProperty("tenant"))
					.authenticate();
			SWIFT_CONTAINER_NAME = prop.getProperty("containerName");
			PM = new PluginsManager(new File(prop.getProperty("pluginsDir")));
			READINESS = Integer.parseInt(prop.getProperty("readiness"));
			LOG_SIZE = Integer.parseInt(prop.getProperty("logSize"));
			SCHED_EXEC_SERV = Executors.newScheduledThreadPool(Integer.parseInt(prop.getProperty("poolSize")));
		}
	}
	/*********************************************************************************************		
	 *Starts monitoring session by loading the plugins and running them. It keeps "taking" from
	 *the BlockingQueue the outputs of the plugins and writes the logs using storeInLocal(). It also
	 *schedules a task which uploads the last logs modified to Swift.
	 */
	public void startMonitoring (){
		PM.loadPlugins();
		PM.runPlugins(SCHED_EXEC_SERV);
		BlockingQueue<JMOMessage> queue = PM.getResultsQueue();
		
		try {
			SCHED_EXEC_SERV.scheduleAtFixedRate(new LogsUploader(PENDING_LOGS, OS.getAccess(), SWIFT_CONTAINER_NAME, HOSTNAME)
					, 0, READINESS, TimeUnit.SECONDS);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		File f = null;

		System.out.println("Monitoring session started.\n"
				+ "Enter q to finish.");

		try(Reader isr = new InputStreamReader (System.in)){
			do{
				f = storeInLocal(queue.take());
				PENDING_LOGS.add(f);
			}while (!isr.ready() || (isr.ready() && isr.read() != 'q'));

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		SCHED_EXEC_SERV.shutdown();
	}
	/********************************************************************************************
	 * Formats a given InputStream by adding the date.
	 * @param is - an InputStream to be formatted
	 * @return The formatted InputStream
	 */
	private InputStream formatResult (InputStream is) {
		String str = null;
		//Just a Scanner trick to convert InputStream to String
		try(Scanner scan = new java.util.Scanner(is)) {
			scan.useDelimiter("\\A");
			str = scan.hasNext() ? scan.next() : ""; 
		}
		SimpleDateFormat sdt = new SimpleDateFormat("yy-MM-dd_HH:mm:ss");
		NOW.setTime(System.currentTimeMillis());
		str = sdt.format(NOW) + ":\n" + str + "\n";
		is = new ByteArrayInputStream(str.getBytes());
		return is;
	}
	/**********************************************************************************************	
	 * Creates the log file locally, creating a new one if the current is bigger than LOG_SIZE.
	 * @param msg - a JMOMessage generated by a plugin. 
	 * @return the log file created/updated.
	 */
	private File storeInLocal (JMOMessage msg) {
		File f = msg.getPlg().getCurrentLog();
		//if file doesn't exist, create it. If it exists check its size
		if(f == null){
			f = createLog(msg);
			f.getParentFile().mkdirs();
			msg.getPlg().setCurrentLog(f);
		}else{
			if(f.length() > LOG_SIZE){
				f = createLog(msg);
				msg.getPlg().setCurrentLog(f);
			}
		}
		
		//Opens the file in append mode and writes the msg's payload
		try (FileOutputStream outstrm = new FileOutputStream (f,true)){
			InputStream is = msg.getPayload();
			is = formatResult(is);
			byte [] b = new byte [is.available()];
			is.read(b);
			outstrm.write(b);
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return f;
	}
	/********************************************************************************************
	 * Create a new File log for the plugin specified in the msg.getPlg() field.
	 * @param msg - JMOMessage generated by the plugin
	 * @return The File log created.
	 */
	private File createLog(JMOMessage msg) {
		File f;
		String plgName = msg.getPlg().getName();
		NOW.setTime(System.currentTimeMillis());
		SimpleDateFormat sdt = new SimpleDateFormat("yy-MM-dd_HH:mm:ss_");
		f = new File (LOCAL_DIR + File.separator + plgName + File.separator+ sdt.format(NOW) +plgName + "_" + HOSTNAME +".txt");
		return f;
	}
	/********************************************************************************************
	 * Return the instance of JMOCore. Successive invocation of this method will return the same
	 * instance.
	 * @return The only instance of JMOCore.
	 */
	public synchronized static JMOCore getInstance(){
		if(INSTANCE == null){
			try {
				return new JMOCore();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return INSTANCE;
	}
}