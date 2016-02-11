package org.jmo;
import java.io.File;
import java.util.concurrent.BlockingQueue;

abstract public class IfcPlugin implements Runnable{
	protected String name;
	protected int rate;
	// this is the reference to the BlockingQueue where all the plugins puts the output of their monitor activity.
	protected BlockingQueue<JMOMessage> monitorQueue;
	// fileCounter keeps track of the # of files written. Starts from 0.
	private File currentLog;
	//*******************************Constructors************************************************
	public IfcPlugin (BlockingQueue<JMOMessage> q) {
		monitorQueue = q;
		currentLog = null;
	}
	/********************************************************************************************
	 * Use this method to set the plugin name and the rate at which call the monitoring() method.
	 * Note: rate - seconds. It must be greater than 0.
	 */
	abstract public void initPlugin ();
	//*******************************Accessor Methods********************************************
	public String getName() {
		return name;
	}
	public long getRate() {
		return rate;
	}
	public void setCurrentLog (File x) { 
		currentLog = x;
	}
	public File getCurrentLog(){
		return currentLog;
	}
}
