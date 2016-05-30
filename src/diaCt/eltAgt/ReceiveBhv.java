package diaCt.eltAgt;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

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
                boolean toForward = false;
                
                Map<String, String> map = JSON.deserializeStringMap(message.getContent());
                List<String> path = null;
                if (map.containsKey(Messaging.PATH)) {
                    path = JSON.deserializeStringList(map.get(Messaging.PATH));
                }
                
                // PUT : add new element
                if (map.get(Messaging.TYPE).equals(Method.PUT.toString())) {
                    if (parentAgt.eltPath.size() == path.size()-1) {
                        String newElt = path.get(path.size()-1);
                        if (parentAgt.sonsElt.containsKey(newElt)) {
                            Errors.throwKO("Element '"+newElt+"' already exists in '"+String.join("/", parentAgt.eltPath)+"'", Errors.NOT_MODIFIED);
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
                        toForward = true;
                    }
                }
                
                // GET : get recursive element description
                else if (map.get(Messaging.TYPE).equals(Method.GET.toString())) {
                    if (parentAgt.eltPath.size() < path.size()) {
                        toForward = true;
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
                            
                            parentAgt.addBehaviour(new WaitDescriptionBhv(parentAgt, message, nbReply));
                        }
                    }
                }
                
                // DELETE : remove the entire diagram, an element and its sub-elements or some properties
                else if (map.get(Messaging.TYPE).equals(Method.DELETE.toString())) {
                    if (map.containsKey(Messaging.PROPERTIES_LIST)) {
                        // remove properties in propertyMap or forward to son
                        if (parentAgt.eltPath.size() >= path.size()) {
                            List<String> propertiesToRemove = JSON.deserializeStringList(map.get(Messaging.PROPERTIES_LIST));
                            for (String p : propertiesToRemove) {
                                parentAgt.propertyMap.remove(p);
                            }
                            toReply = true;
                        }
                        else {
                            toForward = true;
                        }
                    }
                    else {
                        if (parentAgt.eltPath.size() >= path.size()) {
                            // delete current element and forward to every son
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
                            // forward to son
                            String nextElt = path.get(parentAgt.eltPath.size()); // next element in path
                            if (parentAgt.sonsElt.containsKey(nextElt)) {
                                // forward to the next son of the path
                                message.clearAllReceiver();
                                message.addReceiver(parentAgt.sonsElt.get(nextElt));
                                parentAgt.send(message);
                            }
                            else {
                                String status = Errors.NOT_FOUND;
                                if (parentAgt.eltPath.size() == path.size()-1) {
                                    status = Errors.NOT_MODIFIED;
                                }
                                Errors.throwKO("Element '"+nextElt+"' does not exists in '"+String.join("/", parentAgt.eltPath)+"'", status);
                            }
                            
                            // delete link if parent of element to be deleted
                            if (parentAgt.eltPath.size() == path.size()-1) {
                                parentAgt.sonsElt.remove(nextElt );
                            }
                        }
                    }
                }
                
                // POST : modify properties of an element
                else if (map.get(Messaging.TYPE).equals(Method.POST.toString())) {
                    if (parentAgt.eltPath.size() == path.size()) {
                        if (map.containsKey(Messaging.PROPERTIES)) {
                            // property modification
                            Map<String, String> propertyMap = JSON.deserializeStringMap(map.get(Messaging.PROPERTIES));
                            parentAgt.propertyMap.putAll(propertyMap);
                            toReply = true;
                        }
                        else if (map.containsKey(Messaging.OPTIONS)) {
                            List<String> options = JSON.deserializeStringList(map.get(Messaging.OPTIONS));
                            if (options.contains(Messaging.OPT_AUTOPOS)) {
                                // auto-positioning
                                String w = parentAgt.getProperty(EltAgt.W);
                                if (w == null) {
                                    w = "-1";
                                }
                                
                                String h = parentAgt.getProperty(EltAgt.H);
                                if (h == null) {
                                    h = "-1";
                                }
                                
                                // send message to all son with : w, h and an angle
                                ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                                cfp.setConversationId(message.getConversationId());
                                Map<String, String> newMap = new HashMap<>(map);
                                newMap.put(EltAgt.W, w);
                                newMap.put(EltAgt.H, h);
                                cfp.setContent(JSON.serializeStringMap(newMap));
                                
                                int n = parentAgt.sonsElt.size();
                                int i=0;
                                for (AID son : parentAgt.sonsElt.values()) {
                                    newMap.put(EltAgt.A, ""+(2*i*Math.PI/n));
                                    cfp.setContent(JSON.serializeStringMap(newMap));
                                    cfp.clearAllReceiver();
                                    cfp.addReceiver(son);
                                    parentAgt.send(cfp);
                                    i++;
                                }
                                
                                parentAgt.addBehaviour(new WaitProposalBhv(parentAgt, message, n));
                            }
                        }
                    }
                    else {
                        toForward = true;
                    }
                }
                
                // TYPE_RESTORE : restore the states of the elements from a description
                else if (map.get(Messaging.TYPE).equals(Messaging.TYPE_RESTORE)) {
                    Map<String, String> description = JSON.deserializeStringMap(map.get(Messaging.DESCRIPTION));
                    
                    for (String key : description.keySet()) {
                        String value = description.get(key);
                        if (JSON.isJSONObject(value)) {
                            // it's a sub-element
                            // create the new eltAgt
                            List<String> newPath = new ArrayList<>(parentAgt.eltPath);
                            newPath.add(key);
                            
                            AID newElt = Services.addNewElement(parentAgt, newPath, new HashMap<>());
                            parentAgt.sonsElt.put(key, newElt);
                            
                            // and ask him to restore
                            ACLMessage restoreMessage = new ACLMessage(ACLMessage.REQUEST);
                            
                            restoreMessage.addReceiver(newElt);
                            
                            Map<String, String> newMap = new HashMap<>();
                            newMap.put(Messaging.DESCRIPTION, value);
                            newMap.put(Messaging.TYPE, Messaging.TYPE_RESTORE);
                            
                            restoreMessage.setContent(JSON.serializeStringMap(newMap));
                            parentAgt.send(restoreMessage);
                        }
                        else {
                            // it's a property
                            parentAgt.propertyMap.put(key, value);
                        }
                    }
                }
                
                // delete agent, forward or reply to message
                if (toForward) {
                    String nextElt = path.get(parentAgt.eltPath.size()); // next element in path
                    if (parentAgt.sonsElt.containsKey(nextElt)) {
                        // forward to the next son of the path
                        message.clearAllReceiver();
                        message.addReceiver(parentAgt.sonsElt.get(nextElt));
                        parentAgt.send(message);
                    }
                    else {
                        Errors.throwKO("Element '"+nextElt+"' does not exists in '"+String.join("/", parentAgt.eltPath)+"'", Errors.NOT_FOUND);
                    }
                }
                
                if (toReply) {
                    // finished executing the request -> send inform reply
                    ACLMessage reply = message.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    
                    map.put(Messaging.STATUS, Errors.OK);
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
        }
        else{
            block();
        }
    }
}
