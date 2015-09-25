
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
	private BlockingQueue <InputStream> resultsQueue; 
	
	public JmonitorCore (String endpoint, String container, BlockingQueue <InputStream> q, String user, String passwd, String tenant) {
		OPENSTACK_URL_ENDPOINT = endpoint;
		SWIFT_CONTAINER_NAME = container;
		BlockingQueue <InputStream> ResultsQueue = q;
		
		//instance of the Openstack Client
		os = OSFactory.builder()
				.endpoint(endpoint)
				.credentials(user,passwd)
				.tenantName(tenant)
				.authenticate();
		
	}
	
	public void StoreInSwift (){
		os.objectStorage().containers().create(SWIFT_CONTAINER_NAME);
		
		try {
			os.objectStorage().objects().put(SWIFT_CONTAINER_NAME,"cpu_hog.txt" ,
					Payloads.create(resultsQueue.take()), 
					ObjectPutOptions.create()
						.path("/test")
					);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
}
