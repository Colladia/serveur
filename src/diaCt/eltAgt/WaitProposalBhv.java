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

public class WaitProposalBhv extends Behaviour{
    private class Coord {
        public float x;
        public float y;
        public float w;
        public float h;
        
        public Coord(float x, float y, float w, float h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
        
        public boolean intersect(Coord c) {
            if (
                ((c.x >= x  && c.x <= x+w) || (x >= c.x  && x <= c.x+c.w))
                && ((c.y >= y  && c.y <= y+h) || (y >= c.y  && y <= c.y+c.h))
            ) {
                return true;
            }
            return false;
        }
    }
    
    private EltAgt parentAgt;
    private boolean isDone = false;
    private int nbReply;
    private ACLMessage originalMessage;
    private List<Coord> acceptedCoord = new ArrayList<>();

    public WaitProposalBhv(EltAgt parentAgt, ACLMessage originalMessage, int nbReply) {
        this.parentAgt = parentAgt;
        this.originalMessage = originalMessage;
        this.nbReply = nbReply;
    }

    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.and(
            MessageTemplate.MatchConversationId(originalMessage.getConversationId()),
            MessageTemplate.or(
                MessageTemplate.MatchPerformative(ACLMessage.REFUSE),
                MessageTemplate.MatchPerformative(ACLMessage.PROPOSE)
            )
        );
        ACLMessage message = parentAgt.receive(mt);
        if (message != null) {
            if (message.getPerformative() == ACLMessage.REFUSE) {
                nbReply --;
            }
            else {
                Map<String, String> map = JSON.deserializeStringMap(message.getContent());
                
                List<String> options = new ArrayList<>();
                if (map.containsKey(Messaging.OPTIONS)) {
                    options = JSON.deserializeStringList(map.get(Messaging.OPTIONS));
                }
                
                float x = Float.parseFloat(map.get(EltAgt.X));
                float y = Float.parseFloat(map.get(EltAgt.Y));
                float w = Float.parseFloat(map.get(EltAgt.W));
                float h = Float.parseFloat(map.get(EltAgt.H));
                
                Coord c = new Coord(x, y, w, h);
                
                boolean accept = true;
                Coord clash = null;
                if (!options.contains(Messaging.OPT_FORCE)) {
                    for (Coord i : acceptedCoord) {
                        if (c.intersect(i)) {
                            clash = i;
                            accept = false;
                            break;
                        }
                    }
                }
                
                ACLMessage reply = message.createReply();
                if (accept) {
                    acceptedCoord.add(c);
                    reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    nbReply--;
                }
                else {
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    //Map<String, String> newMap = new HashMap<>();
                    //newMap.put(EltAgt.X, ""+clash.x);
                    //newMap.put(EltAgt.Y, ""+clash.y);
                    //newMap.put(EltAgt.W, ""+clash.w);
                    //newMap.put(EltAgt.H, ""+clash.h);
                    //reply.setContent(JSON.serializeStringMap(newMap));
                }
                parentAgt.send(reply);
            }
            
            if (nbReply == 0) {
                isDone = true;
            }
        }
        else{
            block();
        }
    }
    
    @Override
    public boolean done() {
        if (isDone) {
            // reply to the initial request
            Map<String, String> map = JSON.deserializeStringMap(originalMessage.getContent());
            List<String> options = new ArrayList<>();
            if (map.containsKey(Messaging.OPTIONS)) {
                options = JSON.deserializeStringList(map.get(Messaging.OPTIONS));
            }
            // and add a flag to not stock this message in history
            options.add(Messaging.OPT_NOHIST);
            map.put(Messaging.OPTIONS, JSON.serializeStringList(options));
            
            ACLMessage reply = originalMessage.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent(JSON.serializeStringMap(map));
            parentAgt.send(reply);
        }
        
        return isDone;
    }
}
