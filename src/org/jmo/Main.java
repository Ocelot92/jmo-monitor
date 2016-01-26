package org.jmo;

public class Main {
	public static void main (String args []) {
		JMOCore monitor = JMOCore.getInstance();
		monitor.startMonitoring();
	}
}
