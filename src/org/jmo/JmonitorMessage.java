package org.jmo;
import java.io.InputStream;

public class JmonitorMessage {
	private InputStream payload;
	private IfcPlugin plg;
	//*********************************Constructors**********************************************
	public JmonitorMessage (InputStream is, IfcPlugin p){
		payload = is;
		plg = p;
	}
	//*****************************Accessor Methods**********************************************
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
