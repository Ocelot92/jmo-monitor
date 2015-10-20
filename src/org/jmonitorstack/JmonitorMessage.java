package org.jmonitorstack;
import java.io.InputStream;

public class JmonitorMessage {
	private InputStream payload;
	private IfcPlugin plg;
	
	public JmonitorMessage (InputStream is, IfcPlugin p){
		payload = is;
		plg = p;
	}
	
	public void setPlg (IfcPlugin x) {
		plg = x;
	}
	
	public IfcPlugin getPlg () {
		return plg;
	}
	
	public void setPayload (InputStream x) {
		payload = x;
	}
	
	public InputStream getPayload () {
		return payload;
	}
}
