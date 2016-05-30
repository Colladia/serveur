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
                boolean toReply = true;
                boolean toHist = true;
                if (map.containsKey(Messaging.OPTIONS)) {
                    List<String> options = JSON.deserializeStringList(map.get(Messaging.OPTIONS));
                    if (options.contains(Messaging.OPT_NOREPLY)) {
                        toReply = false;
                    }
                    if (options.contains(Messaging.OPT_NOHIST)) {
                        toHist = false;
                    }
                }
                
                // add the modifications to the list if needed
                if (type.equals(Method.PUT.toString()) || type.equals(Method.POST.toString()) || type.equals(Method.DELETE.toString())) {
                    if (toHist) {
                        if (parentAgt.modifications.size() >= parentAgt.MOD_SIZE) {
                            parentAgt.modifications.remove(0);
                            parentAgt.first_clock++;
                        }
                        parentAgt.modifications.add(JSON.serializeStringMap(map));
                    }
                }
                
                if (toReply) {
                    // retrieve last-clock
                    int last_clock = -1;
                    if (map.containsKey(Messaging.LAST_CLOCK)) {
                        last_clock = Integer.parseInt(map.get(Messaging.LAST_CLOCK));
                        map.remove(Messaging.LAST_CLOCK);
                    }
                    
                    if ((last_clock >= parentAgt.first_clock) && (last_clock <= parentAgt.first_clock+parentAgt.modifications.size())) {
                        // return modification list only
                        reply = message.createReply();
                        
                        // send to RestAgt
                        reply.clearAllReceiver();
                        reply.addReceiver(Services.getAgentsByService(parentAgt, Services.REST, Services.REST)[0]);
                        
                        Map<String, String> newMap = new HashMap<>();
                        newMap.put(Messaging.CLOCK, map.get(Messaging.CLOCK));
                        newMap.put(Messaging.STATUS, map.get(Messaging.STATUS));
                        
                        List<String> modList = parentAgt.modifications.subList(last_clock-parentAgt.first_clock, parentAgt.modifications.size());
                        newMap.put(Messaging.MODIFICATION_LIST, JSON.serializeStringList(modList));
                        
                        reply.setContent(JSON.serializeStringMap(newMap));
                    }
                    else {
                        // get complete diagram description
                        Map<String, String> contentMap = new HashMap<>();
                        contentMap.put(Messaging.CLOCK, map.get(Messaging.CLOCK)); // keep clock
                        
                        List<String> path = new ArrayList<>();
                        path.add(parentAgt.diaName);
                        
                        reply = Messaging.getElementDescription(parentAgt, contentMap, path);
                        reply.addReplyTo(Services.getAgentsByService(parentAgt, Services.REST, Services.REST)[0]);
                        reply.setConversationId(message.getConversationId());
                    }
                    parentAgt.send(reply);
                }
            }
            else {
                reply = message.createReply();
                
                // send to RestAgt
                reply.clearAllReceiver();
                reply.addReceiver(Services.getAgentsByService(parentAgt, Services.REST, Services.REST)[0]);
                
                reply.setContent(JSON.serializeStringMap(map));
                parentAgt.send(reply);
                
                // delete agent
                parentAgt.doDelete();
            }
        }
        else{
            block();
        }
    }
}
