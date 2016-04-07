package mainCt.restAgt;

import org.restlet.Server;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import mainCt.restAgt.RestAgt;

public class RestServer extends ServerResource {
    private static RestAgt restAgt = null;
    private static String value = "server-init";
    
    // launch the restlet server
    public static void launchServer(RestAgt agent) {
        if (restAgt == null) {
            try {
                new Server(Protocol.HTTP, 8182, RestServer.class).start();
                restAgt = agent;
            }
            catch (Exception e) {
                System.out.println(e);
            }
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
