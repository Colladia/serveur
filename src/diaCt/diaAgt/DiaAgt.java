package diaCt.diaAgt;

import java.util.Map;
import java.util.HashMap;

import jade.core.Agent;
import jade.core.AID;

import utils.Services;

public class DiaAgt extends Agent {
    public Map<String, String> propertyMap = new HashMap<String, String>();
    public Map<String, DiaElt> subEltMap = new HashMap<String, DiaElt>();
    
    protected void setup() {
        Services.registerService(this, "Diagram", this.getLocalName().substring("DiaAgt-".length()));
        System.out.println(this.getLocalName().substring("DiaAgt-".length()));
    }
}
