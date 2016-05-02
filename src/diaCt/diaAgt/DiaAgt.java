package diaCt.diaAgt;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import utils.Services;
import utils.JSON;

public class DiaAgt extends Agent {
    public DiaElt rootElt = new DiaElt(new HashMap<String, String>());
    public String diaName = null;
    
    protected void setup() {
        diaName = getLocalName().substring("DiaAgt-".length());
        Services.registerService(this, "Diagram", diaName);
        
        addBehaviour(new ReceiveBhv(this));
    }
    
    protected void takeDown() {
        Services.deregisterService(this);
    }
    
    // create a new element and sets its propreties
    public String addNewElement(List<String> path, Map<String, String> propertyMap) {
        String newEltName = path.get(path.size()-1);
        path = path.subList(0, path.size()-1);
        
        DiaElt parentElt = rootElt.retrieveElt(path);
        if (parentElt.subEltMap.containsKey(newEltName)) {
            // element already exists, just send back its content
            propertyMap = parentElt.subEltMap.get(newEltName).propertyMap;
        }
        else {
            // element does not exists, add it
            parentElt.subEltMap.put(newEltName, new DiaElt(propertyMap));
        }
        return JSON.serializeStringMap(propertyMap);
    }
    
    // retrieve the property string of an element
    public String getElementDescription(List<String> path) {
        DiaElt elt = rootElt.retrieveElt(path);
        return elt.getDescription();
    }
    
    // remove an element
    public void rmElement(List<String> path) {
        String toRemove = path.get(path.size()-1);
        path = path.subList(0, path.size()-1);
        
        DiaElt elt = rootElt.retrieveElt(path);
        if (elt.subEltMap.containsKey(toRemove)) {
            elt.subEltMap.remove(toRemove);
        }
        else {
            throw new RuntimeException("Path '"+path+"/"+toRemove+"' does not exists");
        }
    }
    
}
