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
    public static String X = "x";
    public static String Y = "y";
    public static String W = "width";
    public static String H = "height";
    public static String A = "angle";
    
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
        addBehaviour(new ReceiveCFPBhv(this));
    }
    
    protected void takeDown() {
        if (parentElt == null) {
            // if previously registered cause root element
            Services.deregisterService(this);
        }
    }
    
    protected String getProperty(String name) {
        if (propertyMap.containsKey(name)) {
            return propertyMap.get(name);
        }
        else {
            return null;
        }
    }
}
