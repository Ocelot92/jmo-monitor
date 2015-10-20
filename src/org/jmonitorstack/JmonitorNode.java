package org.jmonitorstack;
import java.io.InputStream;

public class JmonitorNode {
	private InputStream payload;
	private IfcPlugin plg;
	
	public JmonitorNode (InputStream is, IfcPlugin p){
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
