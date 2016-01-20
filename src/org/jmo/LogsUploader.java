package org.jmo;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
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
	//************************************Constructors********************************************
	public LogsUploader(Set <File> logsSet, Access acs, String container) {
		PENDING_LOGS = logsSet;
		SWIFT_CONTAINER_NAME = container;
		accessClnt = acs;
	}
	/********************************************************************************************
	 * Uploads all the logs in the pendingLogs set to Swift. 
	 */
	public void uploadLogs () {
		Iterator <File> i = PENDING_LOGS.iterator();
		File f = null;
		
		while (i.hasNext()){
			 f = i.next();
			 String plgname = f.getName().substring(0, f.getName().indexOf('.'));
			 
			 os.objectStorage().containers().create(SWIFT_CONTAINER_NAME);
			 
			 try {
				os.objectStorage().objects().put(SWIFT_CONTAINER_NAME, f.getName(),
						 Payloads.create(f),
						 ObjectPutOptions.create()
						 .path('/' + InetAddress.getLocalHost().getHostName() + '/' + plgname));
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public void run() {
		os = OSFactory.clientFromAccess(accessClnt);
		uploadLogs();
	}

}
