

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import org.jmo.IfcPlugin;
import org.jmo.JMOMessage;


public class PluginCPU extends IfcPlugin{

	public PluginCPU(BlockingQueue<JMOMessage> q, String name) {
		super(q, name);
		rate = 10;
	}

	@Override
	public void run() {
		ProcessBuilder pb = new ProcessBuilder("/bin/sh", "PluginCPU.sh");
		pb.directory(new File(System.getProperty("user.dir") + File.separator + "scripts"));
		Process p = null;
		
		try {
			p = pb.start();
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		sendJMOMessage(p.getInputStream());
	}

}