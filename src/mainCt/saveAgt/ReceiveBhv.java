package mainCt.saveAgt;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Map;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import utils.JSON;
import utils.Messaging;

public class ReceiveBhv extends CyclicBehaviour{
    private SaveAgt parentAgt;
    private File saveDir = new File(parentAgt.SAVE_DIR);

    public ReceiveBhv(SaveAgt parentAgt) {
        this.parentAgt = parentAgt;
    }
    
    @Override
    public void action() {
        // receive the description and save it
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage message = parentAgt.receive(mt);
        if (message != null) {
            Map<String, String> map = JSON.deserializeStringMap(message.getContent());
            
            if (map.containsKey(Messaging.DESCRIPTION)) {
                
                // create directory if it does not exists
                if (! saveDir.exists()) {
                    saveDir.mkdir();
                }
                
                String diaName = message.getSender().getLocalName().substring("DiaAgt-".length());
                
                // write description to a file
                try {
                    PrintWriter writer = new PrintWriter(new File(parentAgt.SAVE_DIR+"/"+diaName+".json"), "UTF8");
                    
                    writer.println(map.get(Messaging.DESCRIPTION));
                    writer.close();
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            block();
        }
    }
}
