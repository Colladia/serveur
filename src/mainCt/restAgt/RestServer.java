package mainCt.restAgt;

import java.lang.String;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.lang.Thread;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

import org.restlet.Server;
import org.restlet.Component;
import org.restlet.util.Series;
import org.restlet.data.Form;
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
    
    // retrieve queryMap from reference
    public Map<String, String> getQueryMap(Reference ref) {
        String query = ref.getQuery();
        if (query == null) {
            return new HashMap<>();
        }
        
        try {
            query = URLDecoder.decode(query, "UTF8");
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to decode query");
        }
        
        return new Form(query).getValuesMap();
    }
    
    // retrieve queryMap from query string
    public Map<String, String> getQueryMap(String query) {
        Map<String, String> queryMap = new HashMap<>();
        
        try {
            query = URLDecoder.decode(query, "UTF8");
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to decode query");
        }
        
        String[] split1 = query.split("&");
        for (String s : split1) {
            String[] split2 = s.split("=");
            queryMap.put(split2[0], split2[1]);
        }
        
        return queryMap;
    }
    
    // split the URI
    public List<String> getSplitPath(Reference ref) {
        List<String> splitPath = Arrays.asList(ref.getPath().substring(1).split("/"));
        if (splitPath.size() < 1) {
            throw new RuntimeException("No diagram specified");
        }
        return splitPath;
    }
    
    // allow PUT, DELETE, GET and POST methods
    @Options()
    public void restOptions() {
        getResponse().setAccessControlAllowOrigin("*");
        
        Set<Method> m = new HashSet();
        m.add(Method.PUT);
        m.add(Method.DELETE);
        m.add(Method.GET);
        m.add(Method.POST);
        getResponse().setAccessControlAllowMethods(m);
        
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
            splitPath = getSplitPath(ref);
            queryMap = getQueryMap(query);
            
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
            List<String> splitPath = getSplitPath(ref);
            Map<String, String> queryMap = getQueryMap(ref);
            
            return ""+splitPath;
        }
        catch (RuntimeException re) {
            return re.getMessage();
        }
    }
}
