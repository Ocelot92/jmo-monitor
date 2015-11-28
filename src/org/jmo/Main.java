package org.jmo;
import java.io.File;

public class Main {
	public static void main (String args []) {
		String endpoint = "http://128.136.179.2:5000/v2.0";
		String container =  "jmonitor-container";
		String user = "facebook961203343944138";
		String passwd = "FtVuoqcq5T2c9dV6";
		String tenant = "facebook961203343944138";
		File dirplg = new File ("plugins");
		
		JmonitorCore monitor = new JmonitorCore(endpoint, container, user, passwd, tenant, dirplg, 0);
		monitor.startMonitoring();
		
	}
}
