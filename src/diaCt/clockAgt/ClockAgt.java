package diaCt.clockAgt;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import utils.Services;

public class ClockAgt extends Agent {
    public String diaName = null;
    public int clock = 0;
    
    protected void setup() {
        diaName = getLocalName().substring("ClockAgt-".length());
        Services.registerService(this, Services.CLOCK, diaName);
        
        addBehaviour(new ReceiveBhv(this));
    }
    
    protected void takeDown() {
        Services.deregisterService(this);
    }
}
