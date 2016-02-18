package org.jmo;
import java.io.File;
import java.util.concurrent.BlockingQueue;

abstract public class IfcPlugin implements Runnable{
	private final String NAME;
	protected int rate;
	// this is the reference to the BlockingQueue where all the plugins puts the output of their monitor activity.
	protected BlockingQueue<JMOMessage> monitorQueue;
	private File currentLog;//the current log file used by the plugin.
	/********************************************************************************************
	 * Creates a IfcPlugin which represents a JMO plugin.
	 * @param q - The BlockingQueue where the plugins puts its JMOMessage objects.
	 */
	public IfcPlugin (BlockingQueue<JMOMessage> q, String name) {
		monitorQueue = q;
		currentLog = null;
		NAME = name;
	}
	/********************************************************************************************
	 * Use this method to set the plugin's rate at which call the monitoring() method.
	 * Note: rate - seconds. It must be greater than 0.
	 */
	abstract public void initPlugin ();
	/********************************************************************************************
	 * Return the plugin's name.
	 * @return a String containing the plugin's name.
	 */
	public String getName() {
		return NAME;
	}
	/********************************************************************************************
	 * Returns the frequency with which the plugin it's executed.
	 * @return Returns the frequency in seconds with which the plugin it's executed.
	 */
	public long getRate() {
		return rate;
	}
	/********************************************************************************************
	 * Sets the current log file of the plugin to x.
	 * @param x - a File representing the last log created by the plugin.
	 */
	public void setCurrentLog (File x) { 
		currentLog = x;
	}
	/********************************************************************************************
	 * Returns the current log file.
	 * @return The last log created by the plugin.
	 */
	public File getCurrentLog(){
		return currentLog;
	}
}
