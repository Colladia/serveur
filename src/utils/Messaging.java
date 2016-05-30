package utils;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.restlet.data.Method;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import utils.JSON;
import utils.Errors;

public class Messaging {
    /* MESSAGE FIELDS */
    public static String DESCRIPTION = "description"; // field containing the properties and sub-element description
    public static String PROPERTIES = "properties"; // field containing the properties of a diagram/element
    public static String TYPE = "type"; // type of query (PUT, GET etc.)
    public static String PATH = "path"; // path to the element
    public static String STATUS = "status"; // status : OK or KO
    public static String ERROR = "error"; // field for error message
    public static String LIST = "diagram-list"; // field for diagram list
    public static String PROPERTIES_LIST = "properties-list"; // field for a list of properties name
    public static String CLOCK = "clock"; // field for returned clock
    public static String LAST_CLOCK = "last-clock"; // field for client last received clock
    public static String MODIFICATION_LIST = "modification-list"; // json array of modifications
    public static String OPTIONS = "options"; // options
    
    /* OPTIONS */
    public static String OPT_AUTOPOS = "auto-positioning";
    public static String OPT_NOREPLY = "no-reply";
    public static String OPT_NOHIST = "no-history";
    public static String OPT_FORCE = "force";
    
    /* REQUEST TYPE */
    // org.restlet.data.Method for PUT, POST, GET and DELETE
    public static String TYPE_RESTORE = "RESTORE"; // new type for query to diaAgt for restoring from a complete description
    
    
    // return a message to create new element and sets its propreties
    public static ACLMessage addNewElement(Agent agent, Map<String, String> contentMap, List<String> path, String propertyMapSerialized) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        
        String diaName = path.get(0);
        message.addReceiver(Services.getDiagram(agent, diaName));
        
        if (contentMap == null) {
            contentMap = new HashMap<>();
        }
        contentMap.put(Messaging.TYPE, Method.PUT.toString());
        contentMap.put(Messaging.PATH, JSON.serializeStringList(path));
        contentMap.put(Messaging.PROPERTIES, propertyMapSerialized);
        
        message.setContent(JSON.serializeStringMap(contentMap));
        return message;
    }
    
    // return a message to restore the elements of a diagram from a serialized map
    public static ACLMessage restoreElements(Agent agent, Map<String, String> contentMap, String diaName, String description) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.addReceiver(Services.getDiagram(agent, diaName));
        
        if (contentMap == null) {
            contentMap = new HashMap<>();
        }
        contentMap.put(Messaging.TYPE, Messaging.TYPE_RESTORE);
        contentMap.put(Messaging.DESCRIPTION, description);
        
        message.setContent(JSON.serializeStringMap(contentMap));
        return message;
    }
    
    // return a message to remove an element or a diagram
    public static ACLMessage rmElement(Agent agent, Map<String, String> contentMap, List<String> path) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        
        String diaName = path.get(0);
        message.addReceiver(Services.getDiagram(agent, diaName));
        
        if (contentMap == null) {
            contentMap = new HashMap<>();
        }
        contentMap.put(Messaging.TYPE, Method.DELETE.toString());
        contentMap.put(Messaging.PATH, JSON.serializeStringList(path));
        
        message.setContent(JSON.serializeStringMap(contentMap));
        return message;
    }
    
    // return a message to remove a list of properties from an element
    public static ACLMessage rmProperties(Agent agent, Map<String, String> contentMap, List<String> path, List<String> propertiesList) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        
        String diaName = path.get(0);
        message.addReceiver(Services.getDiagram(agent, diaName));
        
        if (contentMap == null) {
            contentMap = new HashMap<>();
        }
        contentMap.put(Messaging.TYPE, Method.DELETE.toString());
        contentMap.put(Messaging.PATH, JSON.serializeStringList(path));
        contentMap.put(Messaging.PROPERTIES_LIST, JSON.serializeStringList(propertiesList));
        
        message.setContent(JSON.serializeStringMap(contentMap));
        return message;
    }
    
    // return a message to change/add properties of an element
    public static ACLMessage chProperties(Agent agent, Map<String, String> contentMap, List<String> path, Map<String, String> propertyMap) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        
        String diaName = path.get(0);
        message.addReceiver(Services.getDiagram(agent, diaName));
        
        if (contentMap == null) {
            contentMap = new HashMap<>();
        }
        contentMap.put(Messaging.TYPE, Method.POST.toString());
        contentMap.put(Messaging.PATH, JSON.serializeStringList(path));
        contentMap.put(Messaging.PROPERTIES, JSON.serializeStringMap(propertyMap));
        
        message.setContent(JSON.serializeStringMap(contentMap));
        return message;
    }
    
    // return a message to get the recursive description of a diagram/element
    public static ACLMessage getElementDescription(Agent agent, Map<String, String> contentMap, List<String> path) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        
        String diaName = path.get(0);
        message.addReceiver(Services.getDiagram(agent, diaName));
        
        if (contentMap == null) {
            contentMap = new HashMap<>();
        }
        contentMap.put(Messaging.TYPE, Method.GET.toString());
        contentMap.put(Messaging.PATH, JSON.serializeStringList(path));
        
        message.setContent(JSON.serializeStringMap(contentMap));
        return message;
    }
    
    // return a message to launch an autopositioning algorithm at a given path
    public static ACLMessage autoPositioning(Agent agent, Map<String, String> contentMap, List<String> path) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        
        String diaName = path.get(0);
        message.addReceiver(Services.getDiagram(agent, diaName));
        
        if (contentMap == null) {
            contentMap = new HashMap<>();
        }
        contentMap.put(Messaging.TYPE, Method.POST.toString());
        contentMap.put(Messaging.PATH, JSON.serializeStringList(path));
        
        message.setContent(JSON.serializeStringMap(contentMap));
        return message;
    }
    
}
