package mainCt.restAgt;

import java.lang.String;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
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

import mainCt.restAgt.RestAgt;

public class RestServer extends ServerResource {
    private static RestAgt restAgt = null;
    
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
    
    public static Map<String, String> getQueryMap(Reference ref) {
        String query = ref.getQuery();
        if (query == null) {
            return null;
        }
        
        try {
            query = URLDecoder.decode(query, "UTF8");
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to decode query");
        }
        return new Form(query).getValuesMap();
    }
    
    public static String[] getSplitPath(Reference ref) {
        String[] splitPath = ref.getPath().substring(1).split("/");
        if (splitPath.length < 1) {
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
    }
    
    @Put()
    public String restPut() {
        getResponse().setAccessControlAllowOrigin("*");
        Reference ref = getReference();
        
        String[] splitPath = null;
        try {
            splitPath = getSplitPath(ref);
            
            if (splitPath.length == 1) {
                // create new diagram
                restAgt.addNewDiagram(splitPath[0]);
            }
            else {
                // add new element
                
            }
        }
        catch (RuntimeException re) {
            return re.getMessage();
        }
        
        return "OK";
    }
    
    @Get()
    public String restGet() {  
        getResponse().setAccessControlAllowOrigin("*");
        Reference ref = getReference();
        
        try {
            String[] splitPath = getSplitPath(ref);
            Map<String, String> queryMap = getQueryMap(ref);
        }
        catch (RuntimeException re) {
            return re.getMessage();
        }
        
        return "OK";
    }
}
