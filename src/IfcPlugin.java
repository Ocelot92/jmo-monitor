import java.util.concurrent.BlockingQueue;

abstract class IfcPlugin {
	private final String NAME;
	private final long RATE;
	private final BlockingQueue<JmonitorNode> monitorQueue;
	
	public IfcPlugin (BlockingQueue<JmonitorNode> q) {
		monitorQueue = q;
		NAME = null;
		RATE = 0;
	}
	
	abstract public void monitoring ();
	
	public String getName() {
		return NAME;
	}
	
	public long getRate() {
		return RATE;
	}
	
//Use this method to set the name and the rate at which exec the monitoring() method
	abstract public void initPlugin (); 
}
