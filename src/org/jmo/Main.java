package org.jmo;
import java.io.IOException;


public class Main {
	public static void main (String args []) {
		JMOCore monitor;
		try {
			monitor = new JMOCore();
			monitor.startMonitoring();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
