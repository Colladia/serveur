package mainCt.restAgt;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.net.URLDecoder;

import org.restlet.data.Form;
import org.restlet.data.Reference;

import utils.Messaging;
import utils.Errors;

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
            Errors.throwKO("Unable to decode query", Errors.INTERNAL_ERROR);
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
                Errors.throwKO("Unable to decode query", Errors.INTERNAL_ERROR);
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
            Errors.throwKO("No diagram specified", Errors.BAD_REQUEST);
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
