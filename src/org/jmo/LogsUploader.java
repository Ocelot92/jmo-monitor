package org.jmo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.Payloads;
import org.openstack4j.model.identity.Access;
import org.openstack4j.model.storage.object.options.ObjectPutOptions;
import org.openstack4j.openstack.OSFactory;

public class LogsUploader implements Runnable{
	private final Set <File> PENDING_LOGS;
	private OSClient os;
	private final String SWIFT_CONTAINER_NAME;
	private Access accessClnt;
	private final String HOSTNAME;
	/********************************************************************************************
	 * Creates a LogsUploader which uploads locally modified/created logs to Swift at rate READINESS.
	 * @param logsSet - a Set representing the logs modified or just created.
	 * @param acs - an Access object from OSClient.
	 * @param container - a Swift container name.
	 * @throws IOException - If an I/O error occurs regarding /bin/hostname
	 */ 
	public LogsUploader(Set <File> logsSet, Access acs, String container) throws IOException {
		PENDING_LOGS = logsSet;
		SWIFT_CONTAINER_NAME = container;
		accessClnt = acs;
		InputStream is = Runtime.getRuntime().exec("/bin/hostname").getInputStream();
		
		try(Scanner scan = new Scanner(is)) {
			scan.useDelimiter("\\A");
			HOSTNAME = scan.hasNext() ? scan.next().trim() : ""; 
		}
	}
	/********************************************************************************************
	 * Uploads all the logs in the pendingLogs set to Swift. 
	 */
	public void uploadLogs () {
		File f = null;
		//check if container already exists
		if ( os.objectStorage().containers().getMetadata(SWIFT_CONTAINER_NAME).get("X-Timestamp") == null )
			os.objectStorage().containers().create(SWIFT_CONTAINER_NAME);
		//upload all the Files in PENDING_LOGS to Swift
		synchronized (PENDING_LOGS) {
			Iterator <File> i = PENDING_LOGS.iterator();
			while (i.hasNext()){
				f = i.next();
				String plgname = f.getParent();

				os.objectStorage().objects().put(SWIFT_CONTAINER_NAME, f.getName(),
						Payloads.create(f),
						ObjectPutOptions.create()
						.path('/' + HOSTNAME + '/' + plgname));
				i.remove();
			}
		}
	}
	/********************************************************************************************
	 * Gets the OSClient from OSFactory and call the method uploadLogs().
	 */
	@Override
	public void run() {
		os = OSFactory.clientFromAccess(accessClnt);
		uploadLogs();
	}

}
