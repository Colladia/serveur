package mainCt.saveAgt;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class SaveAgt extends Agent {
    private static int DELAY = 1000; // ms
    public static String SAVE_DIR = "data";
    
    private AgentContainer diaContainer = null;
    
    protected void setup() {
        // retrieve diagram container
        Object[] args = getArguments();
        diaContainer = (AgentContainer) args[0];
        
        addBehaviour(new ReceiveBhv(this));
        addBehaviour(new TickerBhv(this, DELAY));
    }
}
