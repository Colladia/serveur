package diaCt.eltAgt;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.restlet.data.Method;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import utils.JSON;
import utils.Messaging;
import utils.Errors;
import utils.Services;

public class ReceiveBhv extends CyclicBehaviour{

    private EltAgt parentAgt;

    public ReceiveBhv(EltAgt parentAgt) {
        this.parentAgt = parentAgt;
    }

    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        ACLMessage message = parentAgt.receive(mt);
        if (message != null) {
            try {
                boolean toDelete = false;
                boolean toReply = false;
                boolean toTransfer = false;
                
                Map<String, String> map = JSON.deserializeStringMap(message.getContent());
                List<String> path = JSON.deserializeStringList(map.get(Messaging.PATH));
                
                // PUT : add new element
                if (map.get(Messaging.TYPE).equals(Method.PUT.toString())) {
                    if (parentAgt.eltPath.size() == path.size()-1) {
                        String newElt = path.get(path.size()-1);
                        if (parentAgt.sonsElt.containsKey(newElt)) {
                            Errors.throwKO("Element '"+newElt+"' already exists in '"+String.join("/", parentAgt.eltPath)+"'");
                        }
                        else {
                            // add a new son element
                            Map<String, String> propertyMap = JSON.deserializeStringMap(map.get(Messaging.PROPERTIES));
                            AID newEltAID = Services.addNewElement(parentAgt, path, propertyMap);
                            parentAgt.sonsElt.put(newElt, newEltAID);
                            toReply = true;
                        }
                    }
                    else if (parentAgt.eltPath.size() < path.size()-1) {
                        toTransfer = true;
                    }
                }
                
                // GET : get recursive element description
                else if (map.get(Messaging.TYPE).equals(Method.GET.toString())) {
                    if (parentAgt.eltPath.size() < path.size()) {
                        toTransfer = true;
                    }
                    else {
                        if (parentAgt.sonsElt.size() == 0) {
                            // leaf -> return propertyMap
                            map.put(Messaging.DESCRIPTION, JSON.serializeStringMap(parentAgt.propertyMap));
                            toReply = true;
                        }
                        else {
                            int nbReply = parentAgt.sonsElt.size();
                            ACLMessage requestDescMsg = new ACLMessage(ACLMessage.REQUEST);
                            requestDescMsg.setContent(JSON.serializeStringMap(map));
                            requestDescMsg.setConversationId(message.getConversationId());
                            for (AID son : parentAgt.sonsElt.values()) {
                                requestDescMsg.addReceiver(son);
                            }
                            parentAgt.send(requestDescMsg);
                            
                            parentAgt.addBehaviour(new WaitDescription(parentAgt, message, nbReply));
                        }
                    }
                }
                
                // DELETE : remove the entire diagram, an element and its sub-elements or some properties
                else if (map.get(Messaging.TYPE).equals(Method.DELETE.toString())) {
                    if (map.containsKey(Messaging.PROPERTIES_LIST)) {
                        // remove properties in propertyMap or transfer to son
                        if (parentAgt.eltPath.size() >= path.size()) {
                            List<String> propertiesToRemove = JSON.deserializeStringList(map.get(Messaging.PROPERTIES_LIST));
                            for (String p : propertiesToRemove) {
                                parentAgt.propertyMap.remove(p);
                            }
                            toReply = true;
                        }
                        else {
                            toTransfer = true;
                        }
                    }
                    else {
                        if (parentAgt.eltPath.size() >= path.size()) {
                            // delete current element and transfer to every son
                            toDelete = true;
                            
                            message.clearAllReceiver();
                            for (AID son : parentAgt.sonsElt.values()) {
                                message.addReceiver(son);
                            }
                            parentAgt.send(message);
                            
                            // if target of the delete request -> send the reply
                            if (parentAgt.eltPath.size() == path.size()) {
                                toReply = true;
                            }
                        }
                        else {
                            // transfer to son
                            String nextElt = path.get(parentAgt.eltPath.size()); // next element in path
                            if (parentAgt.sonsElt.containsKey(nextElt)) {
                                // transfer to the next son of the path
                                message.clearAllReceiver();
                                message.addReceiver(parentAgt.sonsElt.get(nextElt));
                                parentAgt.send(message);
                            }
                            else {
                                Errors.throwKO("Element '"+nextElt+"' does not exists in '"+String.join("/", parentAgt.eltPath)+"'");
                            }
                            
                            // delete link if parent of element to be deleted
                            if (parentAgt.eltPath.size() == path.size()-1) {
                                parentAgt.sonsElt.remove(nextElt );
                            }
                        }
                    }
                }
                
                if (toTransfer) {
                    String nextElt = path.get(parentAgt.eltPath.size()); // next element in path
                    if (parentAgt.sonsElt.containsKey(nextElt)) {
                        // transfer to the next son of the path
                        message.clearAllReceiver();
                        message.addReceiver(parentAgt.sonsElt.get(nextElt));
                        parentAgt.send(message);
                    }
                    else {
                        Errors.throwKO("Element '"+nextElt+"' does not exists in '"+String.join("/", parentAgt.eltPath)+"'");
                    }
                }
                
                if (toReply) {
                    // finished executing the request -> send inform reply
                    ACLMessage reply = message.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    
                    map.put(Messaging.STATUS, Messaging.OK);
                    reply.setContent(JSON.serializeStringMap(map));
                    
                    parentAgt.send(reply);
                }
                
                if (toDelete) {
                    // delete current agent
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
            
            /**
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
                    
                    //String desc = parentAgt.addNewElement(path, propertyMap);
                    String desc = "";
                    
                    map.put(Messaging.PROPERTIES, desc);
                    
                    reply.setContent(JSON.serializeStringMap(map));
                }
                
                // GET : get element description
                else if (map.get(Messaging.TYPE).equals(Method.GET.toString())) {
                    List<String> completePath = JSON.deserializeStringList(map.get(Messaging.PATH));
                    List<String> path = completePath.subList(1, completePath.size());
                    
                    //String desc = parentAgt.getElementDescription(path);
                    String desc = "";
                    
                    map.put(Messaging.DESCRIPTION, desc);
                    reply.setContent(JSON.serializeStringMap(map));
                }
                
                // DELETE : remove the entire diagram, an element and its sub-elements or some properties
                else if (map.get(Messaging.TYPE).equals(Method.DELETE.toString())) {
                    List<String> completePath = JSON.deserializeStringList(map.get(Messaging.PATH));
                    List<String> path = completePath.subList(1, completePath.size());
                    
                    if (map.containsKey(Messaging.PROPERTIES_LIST)) {
                        // remove properties
                        //parentAgt.rmProperties(path, JSON.deserializeStringList(map.get(Messaging.PROPERTIES_LIST)));
                    }
                    else {
                        // remove diagram/element
                        if (completePath.size() == 1) {
                            toDelete = true;
                        }
                        else {
                            // remove element
                            //parentAgt.rmElement(path);
                        }
                    }
                    
                    reply.setContent(JSON.serializeStringMap(map));
                }
                
                // POST : modify properties of an element
                else if (map.get(Messaging.TYPE).equals(Method.POST.toString())) {
                    List<String> completePath = JSON.deserializeStringList(map.get(Messaging.PATH));
                    List<String> path = completePath.subList(1, completePath.size());
                    
                    //parentAgt.chProperties(path, JSON.deserializeStringMap(map.get(Messaging.PROPERTIES)));
                    
                    reply.setContent(JSON.serializeStringMap(map));
                }
                
                // TYPE_RESTORE : restore the states of the elements from a description
                else if (map.get(Messaging.TYPE).equals(Messaging.TYPE_RESTORE)) {
                    // restore
                    Map<String, String> description = JSON.deserializeStringMap(map.get(Messaging.DESCRIPTION));
                    //parentAgt.rootElt.restoreElements(description);
                    
                    // get and return new complete description
                    //map.put(Messaging.DESCRIPTION, parentAgt.getElementDescription(new ArrayList<>()));
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
            **/
        }
        else{
            block();
        }
    }
}
