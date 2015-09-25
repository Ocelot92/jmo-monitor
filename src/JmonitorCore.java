
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.Payloads;
import org.openstack4j.model.storage.object.options.ObjectPutOptions;
import org.openstack4j.openstack.OSFactory;

public class JmonitorCore {
	private String OPENSTACK_URL_ENDPOINT;
	private String CONTAINER_NAME;
	private OSClient os;
	private String SWIFT_CONTAINER_NAME;
	private BlockingQueue ResultQueue; 
	
	public JmonitorCore () {
		
	}
	
	
	
}
