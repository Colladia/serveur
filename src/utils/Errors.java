package utils;

import java.util.Map;
import java.util.HashMap;

import utils.JSON;
import utils.Messaging;

public class Errors {
    // Success: 2xx
    public static String OK = "200";
    
    // Redirection : 3xx
    public static String NOT_MODIFIED = "304";
    
    // Client error: 4xx
    public static String BAD_REQUEST = "400";
    public static String EXISTS = "401";
    public static String NOT_FOUND = "404";
    
    // Server error: 5xx
    public static String INTERNAL_ERROR = "500";
    
    
    // throw an exception
    public static void throwKO(String msg, String status) {
        Map<String, String> map = new HashMap<>();
        map.put(Messaging.STATUS, status);
        map.put(Messaging.ERROR, msg);
        
        RuntimeException re = new RuntimeException(JSON.serializeStringMap(map));
        
        // DEBUG
        re.printStackTrace();
        System.out.println();
        
        throw re;
    }
}
