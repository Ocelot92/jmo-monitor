

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import org.jmo.IfcPlugin;
import org.jmo.JMOMessage;


public class PluginRAM extends IfcPlugin{

	public PluginRAM(BlockingQueue<JMOMessage> q, String name) {
		super(q, name);
		rate = 20;
	}

	@Override
	public void run() {
        Process p = null;
		try {
			p = Runtime.getRuntime().exec("/usr/bin/free");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		sendJMOMessage(p.getInputStream());
	}

}