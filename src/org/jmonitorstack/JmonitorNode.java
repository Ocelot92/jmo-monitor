package org.jmonitorstack;
import java.io.InputStream;

public class JmonitorNode {
	private InputStream payload;
	private String plgName;
	
	public JmonitorNode (InputStream is, String name){
		payload = is;
		plgName = name;
	}
	
	public void setPlgName (String x) {
		plgName = x;
	}
	
	public String getPlgName () {
		return plgName;
	}
	
	public void setPayload (InputStream x) {
		payload = x;
	}
	
	public InputStream getPayload () {
		return payload;
	}
}
