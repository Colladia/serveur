package utils;

import java.util.Map;
import java.util.HashMap;

import utils.JSON;
import utils.Messaging;

public class Errors {
    public static void throwKO(String msg) {
        Map<String, String> map = new HashMap<>();
        map.put(Messaging.STATUS, Messaging.KO);
        map.put(Messaging.ERROR, msg);
        
        RuntimeException re = new RuntimeException(JSON.serializeStringMap(map));
        
        // DEBUG
        re.printStackTrace();
        
        throw re;
    }
}
