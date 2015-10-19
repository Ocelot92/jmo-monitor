package org.jmonitorstack;
import java.util.TimerTask;

public class PluginScheduler extends TimerTask {
	private IfcPlugin plugin;

	public PluginScheduler (IfcPlugin ip) {
		plugin = ip;
	}
	@Override
	public void run() {
		plugin.monitoring();
		
	}
 
}
