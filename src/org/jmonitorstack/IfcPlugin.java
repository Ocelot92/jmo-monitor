package org.jmonitorstack;
import java.util.concurrent.BlockingQueue;

abstract public class IfcPlugin {
	protected String name;
	protected long rate;
	protected BlockingQueue<JmonitorMessage> monitorQueue;
	
	public IfcPlugin (BlockingQueue<JmonitorMessage> q) {
		monitorQueue = q;
	}
	
	abstract public void monitoring ();
	
	public String getName() {
		return name;
	}
	
	public long getRate() {
		return rate;
	}
	
//Use this method to set the plugin name and the rate at which call the monitoring() method
	abstract public void initPlugin (); 
}
