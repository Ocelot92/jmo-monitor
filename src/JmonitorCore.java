
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.Payloads;
import org.openstack4j.model.storage.object.options.ObjectPutOptions;
import org.openstack4j.openstack.OSFactory;

public class JmonitorCore {
	private final String OPENSTACK_URL_ENDPOINT;
	private final String SWIFT_CONTAINER_NAME;
	private OSClient os;
	//The outputs of the plugins' scripts are stored in this queue waiting for being "consumed" by the os client
	private BlockingQueue <JmonitorNode> resultsQueue; 
	
	public JmonitorCore (String endpoint, String container, BlockingQueue <JmonitorNode> q, String user, String passwd, String tenant) {
		OPENSTACK_URL_ENDPOINT = endpoint;
		SWIFT_CONTAINER_NAME = container;
		BlockingQueue <JmonitorNode> ResultsQueue = q;
		
		//instance of the Openstack Client
		os = OSFactory.builder()
				.endpoint(endpoint)
				.credentials(user,passwd)
				.tenantName(tenant)
				.authenticate();
		
	}
	
	public void StoreInSwift (){
		os.objectStorage().containers().create(SWIFT_CONTAINER_NAME);
		JmonitorNode aux = null;
		try {
			aux = resultsQueue.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (aux != null) {
			os.objectStorage().objects().put(SWIFT_CONTAINER_NAME,"cpu_hog.txt" ,
					Payloads.create(aux.getPayload()), 
					ObjectPutOptions.create()
					.path("/test/" + aux.getPlgName())
					);
		} else {
			System.out.println("Error: trying to take a null JmonitorNode");
		}
	}
	
	
}
