package org.jmo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.openstack4j.api.OSClient;
import org.openstack4j.openstack.OSFactory;

public class JMOCore {
	private final String OS_AUTH_ENDPOINT_URL;
	private final String SWIFT_CONTAINER_NAME;
	private final OSClient OS;
	private PluginsManager pm;
	private Date now;
	private int SIZE_LIMIT; //max size of the log in bytes
	private final String LOCAL_DIR;
	private final int READINESS; //rate (in seconds) at which update logs on Swift
	private final ScheduledExecutorService SCHED_EXEC_SERV;
	private final Set<File> PENDING_LOGS; //Logs that haven't been synch to Swift yet
	//******************************Constructors*********************************************
	public JMOCore (String endpoint, String container, String user, String passwd, String tenant, File dirplg, int rdness) {
		OS_AUTH_ENDPOINT_URL = endpoint;
		SWIFT_CONTAINER_NAME = container;
		SIZE_LIMIT = 500000;// 500kB
		now = new Date ();
		SCHED_EXEC_SERV = Executors.newScheduledThreadPool(3);//pool initialized with 3 threads
		pm = new PluginsManager (dirplg);
		OS = OSFactory.builder()
				.endpoint(OS_AUTH_ENDPOINT_URL)
				.credentials(user,passwd)
				.tenantName(tenant)
				.authenticate();
		LOCAL_DIR = "local";
		READINESS = rdness;
		PENDING_LOGS = (Set<File>) Collections.synchronizedSet(new HashSet<File> ());
	}
	/*********************************************************************************************		
	 *Starts monitoring session by loading the plugins and running them. It keeps "taking" from
	 *the BlockingQueue the outputs of the plugins and writes the logs using storeInLocal(). It also
	 *schedules a task which uploads the last logs modified to Swift.
	 */
	public void startMonitoring (){
		pm.loadPlugins();
		pm.runPlugins(SCHED_EXEC_SERV);
		BlockingQueue<JMOMessage> queue = pm.getResultsQueue();
		
		try {
			SCHED_EXEC_SERV.scheduleAtFixedRate(new LogsUploader(PENDING_LOGS, OS.getAccess(), SWIFT_CONTAINER_NAME), 0, READINESS, TimeUnit.SECONDS);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		File f = null;

		System.out.println("Monitoring session started.\n"
				+ "Enter q to finish.");

		try(Reader isr = new InputStreamReader (System.in)){
			do{
				f = storeInLocal(queue.take()); //updates/creates the the log and returns the File
				PENDING_LOGS.add(f); //add the newly created/updated log to the pending logs
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
	 */
	private InputStream formatResult (InputStream is) {
		String str = null;
		//Just a Scanner trick to convert InputStream to String
		try(Scanner scan = new java.util.Scanner(is)) {
			scan.useDelimiter("\\A");
			str = scan.hasNext() ? scan.next() : ""; 
		}
		now.setTime(System.currentTimeMillis());
		str = now + ": \n" + str + "\n";
		is = new ByteArrayInputStream(str.getBytes());
		return is;
	}
	/**********************************************************************************************	
	 * Creates the log file locally, creates a new file if the current is bigger than SIZE_LIMIT
	 *  and return the File reference of the file created.
	 */
	private File storeInLocal (JMOMessage msg) {
		String plgName = msg.getPlg().getName();
		String path = LOCAL_DIR + File.separator + plgName + File.separator + plgName + String.valueOf(msg.getPlg().getFileCounter()) +".txt";
		File f = new File (path);

		//if file doesn't exist, create it. If it exists check its size
		if(!f.exists()){
			f.getParentFile().mkdirs();
		}else{
			if(f.length() > SIZE_LIMIT){
				msg.getPlg().incFileCounter();
				f = new File(path);
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
}
