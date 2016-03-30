package test;

import org.restlet.Server;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class Test extends ServerResource {
	private static String value = "init";
	
	public static void main (String args[]) {
		try {
			new Server(Protocol.HTTP, 8182, Test.class).start();
			
			//Component component = new Component();  
			//component.getServers().add(Protocol.HTTP, 8182);  
			//component.getDefaultHost().attach("/test", Test.class);  
			//component.start();  
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
	
	@Get()
	public String getAccept() {  
		getResponse().setAccessControlAllowOrigin("*");
		return value;
	}
	
	@Post()
	public String postAccept(String input) {
		getResponse().setAccessControlAllowOrigin("*");
		value = input;
		return value;
	}
}
