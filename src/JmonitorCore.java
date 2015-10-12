import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.Payloads;
import org.openstack4j.model.storage.object.options.ObjectPutOptions;
import org.openstack4j.openstack.OSFactory;

public class JmonitorCore {
	private final String OS_AUTH_ENDPOINT_URL;
	private final String SWIFT_CONTAINER_NAME;
	private OSClient os;
	private List <IfcPlugin> plugins;
//The outputs of the plugins' scripts are stored in this queue waiting for being "consumed" by the os client
	private BlockingQueue <JmonitorNode> resultsQueue; 
	
	public JmonitorCore (String endpoint, String container, BlockingQueue <JmonitorNode> q, String user, String passwd, String tenant) {
		OS_AUTH_ENDPOINT_URL = endpoint;
		SWIFT_CONTAINER_NAME = container;
		BlockingQueue <JmonitorNode> ResultsQueue = q;
		
		//instance of the Openstack Client
		os = OSFactory.builder()
				.endpoint(OS_AUTH_ENDPOINT_URL)
				.credentials(user,passwd)
				.tenantName(tenant)
				.authenticate();
		
	}
	
	public void storeInSwift (){//incomplete
		os.objectStorage().containers().create(SWIFT_CONTAINER_NAME);
		JmonitorNode aux = null;

		try {
			aux = resultsQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		os.objectStorage().objects().put(SWIFT_CONTAINER_NAME,"cpu_hog.txt" ,
				Payloads.create(aux.getPayload()), 
				ObjectPutOptions.create()
				.path("/test/" + aux.getPlgName())
				);

		System.out.println("Error: trying to take a null JmonitorNode");
		
	}	
}
