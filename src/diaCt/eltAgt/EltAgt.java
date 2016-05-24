package diaCt.eltAgt;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import utils.Services;
import utils.JSON;
import utils.Errors;
import utils.Services;

public class EltAgt extends Agent {
    public List<String> eltPath = null;
    public AID parentElt = null;
    public Map<String, AID> sonsElt = new HashMap<>();
    public Map<String, String> propertyMap = new HashMap<>();
    
    protected void setup() {
        Object[] args = getArguments();
        
        if (args != null) {
            // normal element
            parentElt = (AID) args[0];
            eltPath = (List<String>) args[1];
            propertyMap.putAll((Map<String, String>) args[2]);
        }
        else {
            // root element -> old DiaAgt
            String diaName = getLocalName().substring("DiaAgt-".length());
            Services.registerService(this, Services.DIAGRAM, diaName);
            
            eltPath = new ArrayList<>();
            eltPath.add(diaName);
        }
        addBehaviour(new ReceiveBhv(this));
    }
    
    protected void takeDown() {
        if (parentElt == null) {
            // if previously registered cause root element
            Services.deregisterService(this);
        }
    }
    
    /*
    
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
        // do not throw error if the last element does not exists
        //else {
            //Errors.throwKO("Path '"+String.join("/", path)+"/"+toRemove+"' does not exists");
        //}
    }
    
    // remove some properties of an element
    public void rmProperties(List<String> path, List<String> propertiesList) {
        DiaElt elt = rootElt.retrieveElt(path);
        
        for (String i : propertiesList) {
            elt.propertyMap.remove(i);
            // do not throw an error if property does not exists
            //if (elt.propertyMap.containsKey(i)) {
                //elt.propertyMap.remove(i);
            //}
            //else {
                //Errors.throwKO("Properties '"+i+"' does not exists at '"+String.join("/", path)+"'");
            //}
        }
    }
    
    // modify properties of an element
    public void chProperties(List<String> path, Map<String, String> propertyMap) {
        DiaElt elt = rootElt.retrieveElt(path);
        
        elt.propertyMap.putAll(propertyMap);
    }
    
    */
}
