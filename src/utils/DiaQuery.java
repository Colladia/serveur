package utils;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.restlet.data.Method;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import utils.JSON;
import utils.Messaging;
import utils.Errors;

public class DiaQuery {
    // create a new diagram agent
    public static void addNewDiagram(Agent agent, AgentContainer diaContainer, String diaName) {
        try {
            Services.getDiagram(agent, diaName);
        }
        catch(RuntimeException re) {
            try {
                // create DiaAgt if it does not exists yet
                AgentController agentCc = diaContainer.createNewAgent("DiaAgt-"+diaName, "diaCt.diaAgt.DiaAgt", null);
                agentCc.start();
            }
            catch (Exception e) {
                Errors.throwKO("Unable to create diagram '"+diaName+"'");
            }
        }
    }
    
    // create a new element and sets its propreties
    public static void addNewElement(Agent agent, String queryId, List<String> path, String propertyMapSerialized) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        
        String diaName = path.get(0);
        message.addReceiver(Services.getDiagram(agent, diaName));
        
        Map<String, String> map = new HashMap<>();
        map.put(Messaging.TYPE, Method.PUT.toString());
        map.put(Messaging.PATH, JSON.serializeStringList(path));
        map.put(Messaging.PROPERTIES, propertyMapSerialized);
        
        message.setContent(JSON.serializeStringMap(map));
        message.setConversationId(queryId);
        agent.send(message);
    }
    
    // restore the elements of a diagram from a serialized map
    public static void restoreElements(Agent agent, String diaName, String description) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.addReceiver(Services.getDiagram(agent, diaName));
        
        Map<String, String> map = new HashMap<>();
        map.put(Messaging.TYPE, Messaging.TYPE_RESTORE);
        map.put(Messaging.DESCRIPTION, description);
        
        message.setContent(JSON.serializeStringMap(map));
        agent.send(message);
    }
}
