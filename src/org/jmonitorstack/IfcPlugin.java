package org.jmonitorstack;
import java.util.concurrent.BlockingQueue;

abstract public class IfcPlugin {
	protected String name;
	protected long rate;
	// this is the reference to the BlockingQueue where all the plugins puts the output of their monitor activity
	protected BlockingQueue<JmonitorMessage> monitorQueue;
	//fileCounter keeps track of the # of files wrote. It's used for naming a new file after the SIZE_LIMIT is reached 
	private int fileCounter;
	
	public IfcPlugin (BlockingQueue<JmonitorMessage> q) {
		monitorQueue = q;
		fileCounter=0;
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
	
	public void incFileCounter () { 
		fileCounter++;
	}
	
	public int getFileCounter (){
		return fileCounter;
	}
}
