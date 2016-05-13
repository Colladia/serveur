package mainCt.saveAgt;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import org.restlet.data.Method;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import utils.JSON;
import utils.Services;
import utils.Messaging;

public class TickerBhv extends TickerBehaviour{
    private SaveAgt parentAgt;

    public TickerBhv(SaveAgt parentAgt, int delay) {
        super(parentAgt, delay);
        this.parentAgt = parentAgt;
    }
    
    @Override
    public void onTick() {
        // periodicaly send message to get the complete description of each diagram
        
        // DEBUG
        System.out.println("save tick");
        
        List<AID> DiagramList = Services.getDiagramList(parentAgt);
        
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        
        for (AID i : DiagramList) {
            message.addReceiver(i);
        }
        
        Map<String, String> map = new HashMap<>();
        map.put(Messaging.TYPE, Method.GET.toString());
        map.put(Messaging.PATH, "[\"\"]");
        message.setContent(JSON.serializeStringMap(map));
        
        parentAgt.send(message);
    }
}
