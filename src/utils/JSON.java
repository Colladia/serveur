package utils;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

public class JSON {
    
    public static String serializeStringMap(Map<String, String> map) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = null;
        
        try {
            jsonString = mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            System.err.println(e);
        }
        return jsonString;
    }
    
    public static Map<String, String> deserializeStringMap (String serialized) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = null;

        try {
            map = mapper.readValue(serialized, new TypeReference<Map<String, String>>(){});
        } catch (IOException e) {
            System.err.println(e);
        }
        return map;
    }
    
    public static Boolean isObject(String jsonString) {
        return jsonString.startsWith("{", 0);
    }
    
    public static String serializeStringList(List<String> array) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = null;
        
        try {
            jsonString = mapper.writeValueAsString(array);
        } catch (JsonProcessingException e) {
            System.err.println(e);
        }
        return jsonString;
    }
    
    public static List<String> deserializeStringList (String serialized) {
        ObjectMapper mapper = new ObjectMapper();
        List<String> array = null;

        try {
            array = mapper.readValue(serialized, new TypeReference<List<String>>(){});
        } catch (IOException e) {
            System.err.println(e);
        }
        return array;
    }
}
