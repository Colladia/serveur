package diaCt.clockAgt;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import utils.Services;

public class ClockAgt extends Agent {
    public String diaName = null;
    
    protected void setup() {
        diaName = getLocalName().substring("ClockAgt-".length());
        Services.registerService(this, Services.HISTORY, diaName);
    }
    
    protected void takeDown() {
        Services.deregisterService(this);
    }
}
