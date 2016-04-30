package diaCt.diaAgt;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.restlet.data.Method;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import utils.JSON;
import utils.Messaging;

public class ReceiveBhv extends CyclicBehaviour{

    private DiaAgt parentAgt;

    public ReceiveBhv(DiaAgt parentAgt) {
        this.parentAgt = parentAgt;
    }

    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        ACLMessage message = parentAgt.receive(mt);
        if (message != null) {
            Map<String, String> map = JSON.deserializeStringMap(message.getContent());
            
            // PUT
            if (map.get(Messaging.TYPE).equals(Method.PUT.toString())) {
                // add new element
                Map<String, String> propertyMap = JSON.deserializeStringMap(map.get(Messaging.PROPERTIES));
                List<String> path = JSON.deserializeStringList(map.get(Messaging.PATH));
                try {
                    parentAgt.addNewElement(path, propertyMap);
                    
                    ACLMessage reply = message.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(message.getContent());
                    parentAgt.send(reply);
                }
                catch (RuntimeException re) {
                    ACLMessage reply = message.createReply();
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent(re.getMessage());
                    parentAgt.send(reply);
                }
            }
            
        }
        else{
            block();
        }
    }



}
