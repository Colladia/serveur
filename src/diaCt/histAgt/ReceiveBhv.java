package diaCt.histAgt;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import org.restlet.data.Method;

import java.util.Iterator;
import jade.core.AID;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import utils.JSON;
import utils.Messaging;
import utils.Services;

public class ReceiveBhv extends CyclicBehaviour{
    private HistAgt parentAgt;

    public ReceiveBhv(HistAgt parentAgt) {
        this.parentAgt = parentAgt;
    }
    
    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage message = parentAgt.receive(mt);
        if (message != null) {
            Map<String, String> map = JSON.deserializeStringMap(message.getContent());
            String type = map.get(Messaging.TYPE);
            boolean toDelete = false;
            
            // if delete diagram message, stop the agent
            if (type.equals(Method.DELETE.toString()) && map.containsKey(Messaging.PATH)) {
                List<String> path = JSON.deserializeStringList(map.get(Messaging.PATH));
                if (path.size() == 1) {
                    toDelete = true;
                }
            }
            
            ACLMessage reply = null;
            if (!toDelete) {
                if (type.equals(Method.PUT.toString()) || type.equals(Method.POST.toString()) || type.equals(Method.DELETE.toString())) {
                    if (parentAgt.modifications.size() >= parentAgt.MOD_SIZE) {
                        parentAgt.modifications.remove(0);
                        parentAgt.first_clock++;
                    }
                    parentAgt.modifications.add(JSON.serializeStringMap(map));
                }
                
                // DEBUG
                //for (String i : parentAgt.modifications) {
                    //System.out.println(i);
                //}
                //System.out.println();
                
                if (map.containsKey(Messaging.LAST_CLOCK) && (Integer.parseInt(map.get(Messaging.LAST_CLOCK)) >= parentAgt.first_clock)) {
                    reply = message.createReply();
                    
                    // send modification list
                    int last_clock = Integer.parseInt(map.get(Messaging.LAST_CLOCK));
                    
                    // send to RestAgt
                    reply.clearAllReceiver();
                    reply.addReceiver(Services.getAgentsByService(parentAgt, Services.REST, Services.REST)[0]);
                    
                    reply.setContent(JSON.serializeStringMap(map));
                }
                else {
                    Map<String, String> contentMap = new HashMap<>();
                    contentMap.put(Messaging.CLOCK, map.get(Messaging.CLOCK)); // keep clock
                    
                    List<String> path = new ArrayList<>();
                    path.add(parentAgt.diaName);
                    
                    reply = Messaging.getElementDescription(parentAgt, contentMap, path);
                    reply.addReplyTo(Services.getAgentsByService(parentAgt, Services.REST, Services.REST)[0]);
                    reply.setConversationId(message.getConversationId());
                }
            }
            else {
                reply = message.createReply();
                
                // send to RestAgt
                reply.clearAllReceiver();
                reply.addReceiver(Services.getAgentsByService(parentAgt, Services.REST, Services.REST)[0]);
                
                reply.setContent(JSON.serializeStringMap(map));
            }
            
            parentAgt.send(reply);
            
            if (toDelete) {
                parentAgt.doDelete();
            }            
        }
        else{
            block();
        }
    }
}
