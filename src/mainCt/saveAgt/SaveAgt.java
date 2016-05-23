package mainCt.saveAgt;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class SaveAgt extends Agent {
    public static int DELAY = 10000; // ms (10s ?)
    public static String SAVE_DIR = "data";
    
    protected void setup() {
        // restore previously saved diagrams
        addBehaviour(new RestoreBhv(this));
    }
}
