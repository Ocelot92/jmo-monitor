package org.jmo;
import java.io.InputStream;

public class JMOMessage {
	private InputStream payload;
	private IfcPlugin plg;
	
	/* -- Constructors -- */
	public JMOMessage (InputStream is, IfcPlugin p){
		payload = is;
		plg = p;
	}
	
	/* -- Accessor Methods -- */
	/**
	 * Returns the plugin.
	 * @return The plugin who created this JMOMessage
	 */
	public IfcPlugin getPlg () {
		return plg;
	}
	
	/**
	 * Returns the payload of the JMOMessage.
	 * @return The InputStream produced by the plugin execution.
	 */
	public InputStream getPayload () {
		return payload;
	}
}
