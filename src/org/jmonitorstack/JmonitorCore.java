package org.jmonitorstack;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.Payloads;
import org.openstack4j.model.storage.block.options.DownloadOptions;
import org.openstack4j.model.storage.object.options.ObjectLocation;
import org.openstack4j.model.storage.object.options.ObjectPutOptions;
import org.openstack4j.openstack.OSFactory;

public class JmonitorCore {
	private final String OS_AUTH_ENDPOINT_URL;
	private final String SWIFT_CONTAINER_NAME;
	private OSClient os;
	private PluginsManager pm;
	private Date now;
	private long SIZE_LIMIT;
	private final String LOCAL_DIR;
	
	public JmonitorCore (String endpoint, String container, String user, String passwd, String tenant, File dirplg) {
		OS_AUTH_ENDPOINT_URL = endpoint;
		SWIFT_CONTAINER_NAME = container;
		SIZE_LIMIT = 500000;// 500kB
		now = new Date ();
		pm = new PluginsManager (dirplg);
		
		os = OSFactory.builder()
				.endpoint(OS_AUTH_ENDPOINT_URL)
				.credentials(user,passwd)
				.tenantName(tenant)
				.authenticate();
		LOCAL_DIR = "local";
	}
	
	private void storeInSwift (File f){
		os.objectStorage().containers().create(SWIFT_CONTAINER_NAME);
		JmonitorMessage msg = null;

		try {
			msg = pm.getResultsQueue().take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		String plgname = msg.getPlg().getName();
		InputStream payload = msg.getPayload();
		payload = formatResult(payload);
		
		os.objectStorage().objects().put(SWIFT_CONTAINER_NAME,plgname +".txt" ,
				Payloads.create(f), 
				ObjectPutOptions.create()
				 .path("/" + plgname)
				);
		
		//close InputStream of the message took from BlockingQueue
		try {
			payload.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Start monitoring session
	public void startMonitoring (){
		pm.loadPlugins();
		pm.runPlugins();
		BlockingQueue<JmonitorMessage> queue = pm.getResultsQueue();
		
		System.out.println("Monitoring session started.\n"
				+ "Type q to finish.");
		while (true){
			try {
				storeInLocal(queue.take());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

	private void endMonitoring() {
		pm.getTmr().cancel();
		pm.getTmr().purge();
	}
	
/*
 * This method format a given InputStream by adding  date information through the "Date now" attribute.
 * ATTENTION: if you close the scanner you'll close the stream too failing the store operation on Swift.
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
	
	/*Creates the log file locally, creates a new file if the current is bigger than SIZE_LIMIT
	 *  and return the File reference of the file created so that it can be stored in Swift.
	 */
	private File storeInLocal (JmonitorMessage msg) {//fix "local"
		String plgName = msg.getPlg().getName();
		int counter = msg.getPlg().getFileCounter();
		File f = new File (LOCAL_DIR + File.separator + plgName + File.separator + plgName + String.valueOf(counter) +".txt");
		
		//if file doesn't exists, create it. If exists check its size
		if(!f.exists()){
			f.getParentFile().mkdirs();
			}else{
				if(f.length() > SIZE_LIMIT){
				msg.getPlg().incFileCounter();
				f = new File(LOCAL_DIR + plgName + File.separator + plgName + String.valueOf(counter) +".txt");
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
