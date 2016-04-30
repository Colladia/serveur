package mainCt.restAgt;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.restlet.Server;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.Delete;
import org.restlet.resource.Options;
import org.restlet.resource.ServerResource;

import utils.JSON;
import utils.Messaging;
import mainCt.restAgt.RestAgt;
import mainCt.restAgt.RestUtils;

public class RestServer extends ServerResource {
    private static RestAgt restAgt = null;
    public static Map<String, String> returnQueue = new HashMap<String, String>();
    
    // launch the restlet server
    public static void launchServer(RestAgt agent) {
        if (restAgt == null) {
            try {
                new Server(Protocol.HTTP, 8182, RestServer.class).start();
                restAgt = agent;
            }
            catch (Exception e) {
                System.err.println(e);
            }
        }
    }
    
    @Options()
    public void restOptions() {
        // allow origin "*"
        getResponse().setAccessControlAllowOrigin("*");
        
        // allow PUT, DELETE, GET and POST methods
        Set<Method> m = new HashSet();
        m.add(Method.PUT);
        m.add(Method.DELETE);
        m.add(Method.GET);
        m.add(Method.POST);
        getResponse().setAccessControlAllowMethods(m);
        
        // allow json content-type
        Set<String> allowHeaders = getResponse().getAccessControlAllowHeaders();
        allowHeaders.add("content-type");
        getResponse().setAccessControlAllowHeaders(allowHeaders);
    }
    
    @Put("json")
    public String restPut(String query) {
        getResponse().setAccessControlAllowOrigin("*");
        Reference ref = getReference();
        
        List<String> splitPath = null;
        Map<String, String> queryMap = null;
        try {
            splitPath = RestUtils.getSplitPath(ref);
            queryMap = RestUtils.getQueryMap(query);
            
            if (splitPath.size() == 1) {
                // create new diagram
                restAgt.addNewDiagram(splitPath.get(0));
                return "New diagram '"+splitPath.get(0)+"' added";
            }
            else {
                // add new element
                String queryId = UUID.randomUUID().toString();
                
                String propertyMapSerialized = "{}";
                if (queryMap.containsKey(Messaging.PROPERTIES)) {
                    propertyMapSerialized = queryMap.get(Messaging.PROPERTIES);
                }
                
                restAgt.addNewElement(queryId, splitPath, propertyMapSerialized);
                
                int i = 0;
                while (!returnQueue.containsKey(queryId)) {
                    try {
                        Thread.sleep(5);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return returnQueue.remove(queryId);
            }
        }
        catch (RuntimeException re) {
            return re.getMessage();
        }
    }
    
    @Get()
    public String restGet() {  
        getResponse().setAccessControlAllowOrigin("*");
        Reference ref = getReference();
        
        try {
            List<String> splitPath = RestUtils.getSplitPath(ref);
            Map<String, String> queryMap = RestUtils.getQueryMap(ref);
            
            return ""+splitPath;
        }
        catch (RuntimeException re) {
            return re.getMessage();
        }
    }
}
