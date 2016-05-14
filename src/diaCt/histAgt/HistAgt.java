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
    public static int MOD_SIZE = 100;
    
    public String diaName = null;
    public List<String> modifications = new ArrayList<>();
    public int first_clock = 0;
    
    protected void setup() {
        diaName = getLocalName().substring("HistAgt-".length());
        Services.registerService(this, Services.HISTORY, diaName);
        
        addBehaviour(new ReceiveBhv(this));
    }
    
    protected void takeDown() {
        Services.deregisterService(this);
    }
}
