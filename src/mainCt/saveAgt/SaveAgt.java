package mainCt.saveAgt;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class SaveAgt extends Agent {
    public static int DELAY = 60000000; // ms (10s ?)
    public static String SAVE_DIR = "data";
    
    public AgentContainer diaContainer = null;
    
    protected void setup() {
        // retrieve diagram container
        Object[] args = getArguments();
        diaContainer = (AgentContainer) args[0];
        
        // restore previously saved diagrams
        addBehaviour(new RestoreBhv(this));
    }
}
