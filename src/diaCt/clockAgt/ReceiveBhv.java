package diaCt.clockAgt;

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import org.restlet.data.Method;

import java.util.Map;
import java.util.List;

import utils.JSON;
import utils.Messaging;
import utils.Services;

public class ReceiveBhv extends CyclicBehaviour{
    private ClockAgt parentAgt;

    public ReceiveBhv(ClockAgt parentAgt) {
        this.parentAgt = parentAgt;
    }
    
    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage message = parentAgt.receive(mt);
        if (message != null) {
            boolean toDelete = false;
            Map<String, String> map = JSON.deserializeStringMap(message.getContent());
            
            boolean toHist = true;
            if (map.containsKey(Messaging.OPTIONS)) {
                List<String> options = JSON.deserializeStringList(map.get(Messaging.OPTIONS));
                if (options.contains(Messaging.OPT_NOHIST)) {
                    toHist = false;
                }
            }
            
            String type = map.get(Messaging.TYPE);
            
            if (type.equals(Method.PUT.toString()) || type.equals(Method.POST.toString()) || type.equals(Method.DELETE.toString())) {
                // increment clock if needed
                if (toHist) {
                    parentAgt.clock++;
                }
                
                // if delete diagram message, stop the agent
                if (type.equals(Method.DELETE.toString()) && map.containsKey(Messaging.PATH)) {
                    List<String> path = JSON.deserializeStringList(map.get(Messaging.PATH));
                    if (path.size() == 1) {
                        toDelete = true;
                    }
                }
            }
                
            if (!toDelete) {
                map.put(Messaging.CLOCK, ""+parentAgt.clock);
            }
            
            ACLMessage reply = message.createReply();
            reply.setContent(JSON.serializeStringMap(map));
            
            reply.clearAllReceiver();
            reply.addReceiver(Services.getHist(parentAgt, parentAgt.diaName));
            
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
