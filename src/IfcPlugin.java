import java.util.concurrent.BlockingQueue;

abstract class IfcPlugin {
	private final String name;
	private final long rate;
	private final BlockingQueue<IfcPlugin> monitorQueue;
	
	public IfcPlugin (String n, long r, BlockingQueue<IfcPlugin> q) {
		name = n;
		rate = r;
		monitorQueue = q;
	}
	
	abstract public void monitoring ();
	
	public String getName() {
		return name;
	}
	
	public long getRate() {
		return rate;
	}
}
