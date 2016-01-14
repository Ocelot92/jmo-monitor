package org.jmo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.Payloads;
import org.openstack4j.model.storage.object.options.ObjectPutOptions;
import org.openstack4j.openstack.OSFactory;

public class JMOCore {
	private final String OS_AUTH_ENDPOINT_URL;
	private final String SWIFT_CONTAINER_NAME;
	private final OSClient OS;
	private PluginsManager pm;
	private Date now;
	private long SIZE_LIMIT; //max size of the log in bytes
	private final String LOCAL_DIR;
	private final long READINESS; //rate (in milliseconds) at which update logs on Swift: 0 default - immediately
	private final ScheduledExecutorService SCHED_EXEC_SERV;
	private final Set<File> PENDING_LOGS;
	//******************************Constructors*********************************************
	public JMOCore (String endpoint, String container, String user, String passwd, String tenant, File dirplg, long rdness) {
		OS_AUTH_ENDPOINT_URL = endpoint;
		SWIFT_CONTAINER_NAME = container;
		SIZE_LIMIT = 500000;// 500kB
		now = new Date ();
		SCHED_EXEC_SERV = Executors.newScheduledThreadPool(3);//pool initializated with 3 threads
		pm = new PluginsManager (dirplg);

		OS = OSFactory.builder()
				.endpoint(OS_AUTH_ENDPOINT_URL)
				.credentials(user,passwd)
				.tenantName(tenant)
				.authenticate();
		LOCAL_DIR = "local";
		READINESS = rdness;
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
		File f = null;
		

		System.out.println("Monitoring session started.\n"
				+ "Enter q to finish.");

		try(Reader isr = new InputStreamReader (System.in)){
			do{
				f = storeInLocal(queue.take());
			}while (!isr.ready() || (isr.ready() && isr.read() != 'q'));

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/********************************************************************************************
	 * Formats a given InputStream by adding the date.
	 * ATTENTION: if you close the scanner you'll close the stream too, failing the store operation.
	 */
	private InputStream formatResult (InputStream is) {
		//Just a Scanner trick to convert InputStream to String
		Scanner scan = new Scanner(is);
		scan.useDelimiter("\\A");
		String str =  scan.hasNext() ? scan.next() : "";

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

		//if file doesn't exists, create it. If exists check its size
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