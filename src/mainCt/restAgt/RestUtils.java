package mainCt.restAgt;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.net.URLDecoder;

import org.restlet.data.Form;
import org.restlet.data.Reference;

import utils.Messaging;

public class RestUtils {
    // retrieve queryMap from reference
    public static Map<String, String> getQueryMap(Reference ref) {
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
    public static Map<String, String> getQueryMap(String query) {
        Map<String, String> queryMap = new HashMap<>();
        
        if (query != null) {
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
        }
        
        return queryMap;
    }
    
    // split the URI
    public static List<String> getSplitPath(Reference ref) {
        List<String> splitPath = Arrays.asList(ref.getPath().substring(1).split("/"));
        if (splitPath.size() < 1) {
            throw new RuntimeException("No diagram specified");
        }
        return splitPath;
    }
    
    // get property map from query map
    public static String getPropertyMap(Map<String, String> queryMap) {
        String propertyMapSerialized = "{}";
        if (queryMap.containsKey(Messaging.PROPERTIES)) {
            propertyMapSerialized = queryMap.get(Messaging.PROPERTIES);
        }
        return propertyMapSerialized;
    }
}
