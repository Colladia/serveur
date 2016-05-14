package diaCt.histAgt;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import utils.Services;

public class HistAgt extends Agent {
    public String diaName = null;
    
    protected void setup() {
        diaName = getLocalName().substring("HistAgt-".length());
        Services.registerService(this, Services.HISTORY, diaName);
    }
    
    protected void takeDown() {
        Services.deregisterService(this);
    }
}
