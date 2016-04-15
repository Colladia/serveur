package diaCt.diaAgt;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import utils.Services;

public class DiaAgt extends Agent {
    public Map<String, String> propertyMap = new HashMap<String, String>();
    public Map<String, DiaElt> subEltMap = new HashMap<String, DiaElt>();
    public String diaName = null;
    
    protected void setup() {
        diaName = getLocalName().substring("DiaAgt-".length());
        Services.registerService(this, "Diagram", diaName);
        
        addBehaviour(new ReceiveBhv(this));
    }
    
    // create a new element and sets its propreties
    public void addNewElement(List<String> path, Map<String, String> propertyMap) {
        if (path.size() > 1) {
            if (this.subEltMap.containsKey(path.get(0))) {
                DiaElt curElt = this.subEltMap.get(path.get(0));
                String pathString = path.get(0);
                
                int i;
                for (i=1; i<path.size()-1; i++) {
                    pathString+="/"+path.get(i);
                    if (curElt.subEltMap.containsKey(path.get(i))) {
                        curElt = curElt.subEltMap.get(path.get(i));
                    }
                    else {
                        throw new RuntimeException("Path '"+pathString+"' does not exists in '"+diaName+"'");
                    }
                }
                curElt.subEltMap.put(path.get(i), new DiaElt(propertyMap));
            }
            else {
                throw new RuntimeException("Path '"+path.get(0)+"' does not exists in '"+diaName+"'");
            }
        }
        else {
            // add new elt in diaAgt
            this.subEltMap.put(path.get(0), new DiaElt(propertyMap));
        }
    }
}
