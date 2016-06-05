package diaCt.eltAgt;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;
import java.lang.Float;

import org.restlet.data.Method;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import utils.JSON;
import utils.Messaging;
import utils.Services;

public class ProposeBhv extends Behaviour{
    public static float STEP = 100;

    private EltAgt parentAgt;
    private boolean isDone = false;
    private boolean init = false;
    private ACLMessage originalMessage;
    private Map<String, String> map;
    
    private float curX;
    private float curY;
    float prevX;
    float prevY;
    private float w;
    private float h;
    private float pW;
    private float pH;
    private float angle;

    public ProposeBhv(EltAgt parentAgt, ACLMessage originalMessage) {
        this.parentAgt = parentAgt;
        this.originalMessage = originalMessage;
        
        this.map = JSON.deserializeStringMap(originalMessage.getContent());
        this.pW = Float.parseFloat(map.get(EltAgt.W));
        this.pH = Float.parseFloat(map.get(EltAgt.H));
        this.angle = Float.parseFloat(map.get(EltAgt.A));
    }

    @Override
    public void action() {
        boolean toRefuse = false;
        boolean toReply = false;
        if (!init) {
            // check if valid element for auto-positioning
            if (parentAgt.getProperty(EltAgt.XMIN)!=null && parentAgt.getProperty(EltAgt.YMIN)!=null && parentAgt.getProperty(EltAgt.XMAX)!=null && parentAgt.getProperty(EltAgt.YMAX)!=null) {
                curX = Float.parseFloat(parentAgt.getProperty(EltAgt.XMIN));
                curY = Float.parseFloat(parentAgt.getProperty(EltAgt.YMIN));
                w = Float.parseFloat(parentAgt.getProperty(EltAgt.XMAX)) - Float.parseFloat(parentAgt.getProperty(EltAgt.XMIN));
                h = Float.parseFloat(parentAgt.getProperty(EltAgt.YMAX)) - Float.parseFloat(parentAgt.getProperty(EltAgt.YMIN));
            }
            else {
                toRefuse = true;
            }
            
            if  (!toRefuse) {
                // init x and y
                if (pW < 0) {
                    // unlimited
                    curX = 0 - w/2;
                }
                else {
                    curX = pW/2 - w/2;
                }
                if (pW >= 0) {
                    if (curX < 0) {
                        curX = 0;
                    }
                    else if (curX+w > pW) {
                        curX = pW - w;
                    }
                }
                
                if (pH < 0) {
                    // unlimited
                    curY = 0 + h/2;
                }
                else {
                    curY = pH/2 + h/2;
                }
                if (pH >= 0) {
                    if (curY < 0) {
                        curY = 0;
                    }
                    else if (curY+h > pH) {
                        curY = pH - h;
                    }
                }
            }
            toReply = true;
            init = true;
        }
        else {
            MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchConversationId(originalMessage.getConversationId()),
                MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
                    MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL)
                )
            );
            ACLMessage message = parentAgt.receive(mt);
            if (message != null) {
                if (message.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                    // change value, send message to history and stop behaviour
                    parentAgt.propertyMap.put(EltAgt.XMIN, ""+curX);
                    parentAgt.propertyMap.put(EltAgt.YMIN, ""+curY);
                    parentAgt.propertyMap.put(EltAgt.XMAX, ""+(curX+w));
                    parentAgt.propertyMap.put(EltAgt.YMAX, ""+(curY+h));
                    isDone = true;
                }
                else {
                    // retrieve clashing element coordinates
                    //Map<String, String> map = JSON.deserializeStringMap(message.getContent());
                    //float clashX = Float.parseFloat(map.get(EltAgt.X));
                    //float clashY = Float.parseFloat(map.get(EltAgt.Y));
                    //float clashW = Float.parseFloat(map.get(EltAgt.W));
                    //float clashH = Float.parseFloat(map.get(EltAgt.H));
                    
                    prevX = curX;
                    prevY = curY;
                    
                    curX += ((float) Math.cos(angle)) * STEP;
                    if (pW >= 0) {
                        if (curX < 0) {
                            curX = 0;
                        }
                        else if (curX+w > pW) {
                            curX = pW - w;
                        }
                    }
                    
                    curY += ((float) Math.sin(angle)) * STEP;
                    if (pH >= 0) {
                        if (curY < 0) {
                            curY = 0;
                        }
                        else if (curY+h > pH) {
                            curY = pH - h;
                        }
                    }
                    
                    toReply = true;
                }
            }
            else {
                block();
            }
        }
        
        if (toReply) {
            ACLMessage reply = originalMessage.createReply();
            if (toRefuse) {
                reply.setPerformative(ACLMessage.REFUSE);
            }
            else {
                reply.setPerformative(ACLMessage.PROPOSE);
                Map<String, String> newMap = new HashMap<>();
                newMap.put(EltAgt.X, ""+curX);
                newMap.put(EltAgt.Y, ""+curY);
                newMap.put(EltAgt.W, ""+w);
                newMap.put(EltAgt.H, ""+h);
                
                if (curX == prevX || curY == prevY) {
                    // no move made cause reached a corner of the parent
                    List<String> options = new ArrayList<>();
                    options.add(Messaging.OPT_FORCE);
                    newMap.put(Messaging.OPTIONS, JSON.serializeStringList(options));
                }
                
                reply.setContent(JSON.serializeStringMap(newMap));
            }
            parentAgt.send(reply);
        }
    }
    
    @Override
    public boolean done() {
        if (isDone) {
            // send message for HistAgt
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.addReceiver(Services.getClock(parentAgt, parentAgt.eltPath.get(0)));
            
            Map<String, String> contentMap = new HashMap<>();
            contentMap.put(Messaging.TYPE, Method.POST.toString());
            contentMap.put(Messaging.PATH, JSON.serializeStringList(parentAgt.eltPath));
            
            Map<String, String> propertyMap = new HashMap<>();
            propertyMap.put(EltAgt.XMIN, ""+curX);
            propertyMap.put(EltAgt.YMIN, ""+curY);
            propertyMap.put(EltAgt.XMAX, ""+(curX+w));
            propertyMap.put(EltAgt.YMAX, ""+(curY+h));
            propertyMap.put(EltAgt.ID, parentAgt.getProperty(EltAgt.ID));
            propertyMap.put(EltAgt.TYPE, parentAgt.getProperty(EltAgt.TYPE));
            contentMap.put(Messaging.PROPERTIES, JSON.serializeStringMap(propertyMap));
            
            List<String> options = new ArrayList<>();
            options.add(Messaging.OPT_AUTOPOS);
            options.add(Messaging.OPT_NOREPLY);
            contentMap.put(Messaging.OPTIONS, JSON.serializeStringList(options));
            
            message.setContent(JSON.serializeStringMap(contentMap));
            
            parentAgt.send(message);
        }
        
        return isDone;
    }
}
