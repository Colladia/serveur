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
            try {
                Map<String, String> map = JSON.deserializeStringMap(message.getContent());
                map.put(Messaging.STATUS, Messaging.OK);
                
                List<String> completePath = JSON.deserializeStringList(map.get(Messaging.PATH));
                List<String> path = completePath.subList(1, completePath.size());
                
                // PUT : add new element
                if (map.get(Messaging.TYPE).equals(Method.PUT.toString())) {
                    Map<String, String> propertyMap = JSON.deserializeStringMap(map.get(Messaging.DESCRIPTION));
                    
                    String desc = parentAgt.addNewElement(path, propertyMap);
                    
                    ACLMessage reply = message.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    
                    map.put(Messaging.DESCRIPTION, desc);
                    reply.setContent(JSON.serializeStringMap(map));
                    
                    parentAgt.send(reply);
                }
                
                // GET : get element description
                else if (map.get(Messaging.TYPE).equals(Method.GET.toString())) {
                    String desc = parentAgt.getElementDescription(path);
                    ACLMessage reply = message.createReply();
                    
                    reply.setPerformative(ACLMessage.INFORM);
                    
                    map.put(Messaging.DESCRIPTION, desc);
                    reply.setContent(JSON.serializeStringMap(map));
                    
                    parentAgt.send(reply);
                }
                
                // DELETE : remove the entire diagram or an element and its sub-elements
                else if (map.get(Messaging.TYPE).equals(Method.DELETE.toString())) {
                    if (completePath.size() == 1) {
                        // stop the current diagram
                        parentAgt.doDelete();
                    }
                    else {
                        // remove element
                        parentAgt.rmElement(path);
                    }
                    
                    ACLMessage reply = message.createReply();
                    
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(JSON.serializeStringMap(map));
                    
                    parentAgt.send(reply);
                }
                
            }
            catch (RuntimeException re) {
                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.FAILURE);
                reply.setContent(re.getMessage());
                parentAgt.send(reply);
            }
        }
        else{
            block();
        }
    }



}
