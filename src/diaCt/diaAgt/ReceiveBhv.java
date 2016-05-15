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
import utils.Services;

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
                boolean toDelete = false;
                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                
                Map<String, String> map = JSON.deserializeStringMap(message.getContent());
                map.put(Messaging.STATUS, Messaging.OK);
                
                // PUT : add new element
                if (map.get(Messaging.TYPE).equals(Method.PUT.toString())) {
                    List<String> completePath = JSON.deserializeStringList(map.get(Messaging.PATH));
                    List<String> path = completePath.subList(1, completePath.size());
                
                    Map<String, String> propertyMap = JSON.deserializeStringMap(map.get(Messaging.PROPERTIES));
                    
                    String desc = parentAgt.addNewElement(path, propertyMap);
                    
                    map.put(Messaging.PROPERTIES, desc);
                    
                    reply.setContent(JSON.serializeStringMap(map));
                }
                
                // GET : get element description
                else if (map.get(Messaging.TYPE).equals(Method.GET.toString())) {
                    List<String> completePath = JSON.deserializeStringList(map.get(Messaging.PATH));
                    List<String> path = completePath.subList(1, completePath.size());
                    
                    String desc = parentAgt.getElementDescription(path);
                    
                    map.put(Messaging.DESCRIPTION, desc);
                    reply.setContent(JSON.serializeStringMap(map));
                }
                
                // DELETE : remove the entire diagram, an element and its sub-elements or some properties
                else if (map.get(Messaging.TYPE).equals(Method.DELETE.toString())) {
                    List<String> completePath = JSON.deserializeStringList(map.get(Messaging.PATH));
                    List<String> path = completePath.subList(1, completePath.size());
                    
                    if (map.containsKey(Messaging.PROPERTIES_LIST)) {
                        // remove properties
                        parentAgt.rmProperties(path, JSON.deserializeStringList(map.get(Messaging.PROPERTIES_LIST)));
                    }
                    else {
                        // remove diagram/element
                        if (completePath.size() == 1) {
                            toDelete = true;
                        }
                        else {
                            // remove element
                            parentAgt.rmElement(path);
                        }
                    }
                    
                    reply.setContent(JSON.serializeStringMap(map));
                }
                
                // POST : modify properties of an element
                else if (map.get(Messaging.TYPE).equals(Method.POST.toString())) {
                    List<String> completePath = JSON.deserializeStringList(map.get(Messaging.PATH));
                    List<String> path = completePath.subList(1, completePath.size());
                    
                    parentAgt.chProperties(path, JSON.deserializeStringMap(map.get(Messaging.PROPERTIES)));
                    
                    reply.setContent(JSON.serializeStringMap(map));
                }
                
                // TYPE_RESTORE : restore the states of the elements from a description
                else if (map.get(Messaging.TYPE).equals(Messaging.TYPE_RESTORE)) {
                    // restore
                    Map<String, String> description = JSON.deserializeStringMap(map.get(Messaging.DESCRIPTION));
                    parentAgt.rootElt.restoreElements(description);
                    
                    // get and return new complete description
                    map.put(Messaging.DESCRIPTION, parentAgt.getElementDescription(new ArrayList<>()));
                    reply.setContent(JSON.serializeStringMap(map));
                }
                
                parentAgt.send(reply);
                
                // delete current agent
                if (toDelete) {
                    parentAgt.doDelete();
                }
            }
            catch (RuntimeException re) {
                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.FAILURE);
                reply.setContent(re.getMessage());
                
                // send error message to initial sender (do not follow reply-to)
                reply.clearAllReceiver();
                reply.addReceiver(message.getSender());
                
                parentAgt.send(reply);
            }
        }
        else{
            block();
        }
    }



}
