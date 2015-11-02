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
	
	public JmonitorCore (String endpoint, String container, String user, String passwd, String tenant, String dirplg) {
		OS_AUTH_ENDPOINT_URL = endpoint;
		SWIFT_CONTAINER_NAME = container;
		now = new Date ();
		pm = new PluginsManager (dirplg);
		
		os = OSFactory.builder()
				.endpoint(OS_AUTH_ENDPOINT_URL)
				.credentials(user,passwd)
				.tenantName(tenant)
				.authenticate();
	}
	
	public void storeInSwift (){
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
				Payloads.create(payload), 
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
	
	public void startMonitoring (){
		pm.loadPlugins();
		pm.runPlugins();
		
		
		System.out.println("Monitoring session started.\n"
				+ "Type q to finish.");
		//while (true)
			storeInSwift();
		
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
	   
	    str = now + ": \n" + str + "\n";
		is = new ByteArrayInputStream(str.getBytes());
		return is;
	}
	
	private File storeInLocalLog (JmonitorMessage msg) { //add to UML
		String plgName = msg.getPlg().getName();
		//create a the file and directory path if not exits
		File f = new File ("plgName" + File.separator + plgName + ".txt");
		if (!f.exists())
		
		try (OutputStream p = new FileOutputStream (plgName)){
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
