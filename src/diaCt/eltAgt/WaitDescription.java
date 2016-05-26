package diaCt.eltAgt;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.restlet.data.Method;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import utils.JSON;
import utils.Messaging;
import utils.Errors;
import utils.Services;

public class WaitDescription extends Behaviour{

    private EltAgt parentAgt;
    private boolean isDone = false;
    private int nbReply;
    private ACLMessage originalMessage;
    private Map<String, String> description;

    public WaitDescription(EltAgt parentAgt, ACLMessage originalMessage, int nbReply) {
        this.parentAgt = parentAgt;
        this.originalMessage = originalMessage;
        this.nbReply = nbReply;
        
        this.description = new HashMap<>();
        description.putAll(parentAgt.propertyMap);
    }

    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchConversationId(originalMessage.getConversationId()));
        ACLMessage message = parentAgt.receive(mt);
        if (message != null) {
            Map<String, String> map = JSON.deserializeStringMap(message.getContent());
            String sonDescription = map.get(Messaging.DESCRIPTION);
            
            String sonName = message.getSender().getLocalName();
            sonName = sonName.substring(sonName.lastIndexOf("/")+1);
            description.put(sonName, sonDescription);
            
            nbReply--;
            if (nbReply <= 0) {
                ACLMessage reply = originalMessage.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                
                map.put(Messaging.STATUS, Errors.SUCCESS);
                map.put(Messaging.DESCRIPTION, JSON.serializeStringMap(description));
                reply.setContent(JSON.serializeStringMap(map));
                
                parentAgt.send(reply);
                isDone = true;
            }
        }
        else{
            block();
        }
    }
    
    @Override
    public boolean done() {
        return isDone;
    }
}
