import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.Payloads;
import org.openstack4j.model.storage.object.options.ObjectPutOptions;
import org.openstack4j.openstack.OSFactory;

public class JmonitorCore {
	private final String OS_AUTH_ENDPOINT_URL;
	private final String SWIFT_CONTAINER_NAME;
	private OSClient os;
	private PluginsManager pm;
	private Date now;
	private String dir = System.getProperty("user.dir") + "/plugins";
	
	public JmonitorCore (String endpoint, String container, BlockingQueue <JmonitorNode> q, String user, String passwd, String tenant) {
		OS_AUTH_ENDPOINT_URL = endpoint;
		SWIFT_CONTAINER_NAME = container;
		now = new Date ();
		pm = new PluginsManager (dir);//directory plugins hardcoded
		os = OSFactory.builder()
				.endpoint(OS_AUTH_ENDPOINT_URL)
				.credentials(user,passwd)
				.tenantName(tenant)
				.authenticate();
	}
	
	public void storeInSwift (){
		os.objectStorage().containers().create(SWIFT_CONTAINER_NAME);
		JmonitorNode node = null;

		try {
			node = pm.getResultsQueue().take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		formatResult(node.getPayload());
		
		String plgname = node.getPlgName();
		os.objectStorage().objects().put(SWIFT_CONTAINER_NAME,plgname +".txt" ,
				Payloads.create(node.getPayload()), 
				ObjectPutOptions.create()
				.path("/" + plgname + "/" + node.getPlgName())
				);
	}
	
//adds date info at the beginning of an InputStream
	private InputStream formatResult (InputStream is) {
		//Just a Scanner trick to convert InputStream to String
		Scanner scan = new Scanner(is).useDelimiter("\\A");
	    String str =  scan.hasNext() ? scan.next() : "";
	    scan.close();
	    str = now + ": " + str;
		is = new ByteArrayInputStream(str.getBytes());
		return is;
	}
}
