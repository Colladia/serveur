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
import utils.Services;
import utils.Errors;
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
    
    // wait the reply of a request
    public String waitReply(String queryId) {
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
    
    @Put()
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
                Services.addNewDiagram(restAgt, splitPath.get(0));
                Map<String, String> map = new HashMap<>();
                map.put(Messaging.TYPE, Method.PUT.toString());
                map.put(Messaging.PATH, JSON.serializeStringList(splitPath));
                map.put(Messaging.STATUS, Errors.SUCCESS);
                return JSON.serializeStringMap(map);
            }
            else {
                // add new element
                String queryId = UUID.randomUUID().toString();
                
                String propertyMapSerialized = RestUtils.getPropertyMap(queryMap);
                queryMap.remove(Messaging.PROPERTIES);
                
                restAgt.addNewElement(queryId, splitPath, propertyMapSerialized, queryMap);
                
                return waitReply(queryId);
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
        
        List<String> splitPath = null;
        try {
            splitPath = RestUtils.getSplitPath(ref);
            if (splitPath.size()==1 && splitPath.get(0).equals("")) {
                // return the list of available diagram
                Map<String, String> map = new HashMap<>();
                map.put(Messaging.TYPE, Method.GET.toString());
                map.put(Messaging.LIST, JSON.serializeStringList(Services.getDiagramNameList(restAgt)));
                map.put(Messaging.STATUS, Errors.SUCCESS);
                return JSON.serializeStringMap(map);
            }
            else {
                // get diagram/element description
                Map<String, String> queryMap = null;
                queryMap = RestUtils.getQueryMap(ref);
                
                String queryId = UUID.randomUUID().toString();
                
                restAgt.getElementDescription(queryId, splitPath, queryMap);
                
                return waitReply(queryId);
            }
        }
        catch (RuntimeException re) {
            return re.getMessage();
        }
    }
    
    @Delete()
    public String restDel(String query) {
        getResponse().setAccessControlAllowOrigin("*");
        Reference ref = getReference();
        
        List<String> splitPath = null;
        Map<String, String> queryMap = null;
        try {
            queryMap = RestUtils.getQueryMap(query);
            splitPath = RestUtils.getSplitPath(ref);
            String queryId = UUID.randomUUID().toString();
            
            List<String> propertiesList = null;
            if (queryMap.containsKey(Messaging.PROPERTIES_LIST)) {
                propertiesList = JSON.deserializeStringList(queryMap.get(Messaging.PROPERTIES_LIST));
                queryMap.remove(Messaging.PROPERTIES_LIST);
            }
            
            if (propertiesList!=null) {
                restAgt.rmProperties(queryId, splitPath, propertiesList, queryMap);
            }
            else {
                restAgt.rmElement(queryId, splitPath, queryMap);
            }
            
            return waitReply(queryId);
        }
        catch (RuntimeException re) {
            return re.getMessage();
        }
    }
    
    @Post()
    public String restPost(String query) {
        getResponse().setAccessControlAllowOrigin("*");
        Reference ref = getReference();
        
        List<String> splitPath = null;
        Map<String, String> queryMap = null;
        try {
            queryMap = RestUtils.getQueryMap(query);
            splitPath = RestUtils.getSplitPath(ref);
            String queryId = UUID.randomUUID().toString();
            
            Map<String, String> propertyMap = JSON.deserializeStringMap(queryMap.get(Messaging.PROPERTIES));
            queryMap.remove(Messaging.PROPERTIES);
            restAgt.chProperties(queryId, splitPath, propertyMap, queryMap);
            
            return waitReply(queryId);
        }
        catch (RuntimeException re) {
            return re.getMessage();
        }
    }
}
