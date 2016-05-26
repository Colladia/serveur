package mainCt.restAgt;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.restlet.data.Method;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import utils.JSON;
import utils.Services;
import utils.Messaging;
import utils.Errors;
import mainCt.restAgt.RestServer;

public class RestAgt extends Agent {
    
    protected void setup() {
        // register
        Services.registerService(this, Services.REST, Services.REST);
        
        RestServer.launchServer(this);
    }
    
    // create a new element and sets its propreties
    public void addNewElement(String queryId, List<String> path, String propertyMapSerialized, Map<String, String> queryMap) {
        ACLMessage msg = Messaging.addNewElement(this, queryMap, path, propertyMapSerialized);
        msg.setConversationId(queryId);
        
        String diaName = path.get(0);
        msg.addReplyTo(Services.getClock(this, diaName));
        
        this.send(msg);
        addBehaviour(new ReceiveBhv(this, queryId));
    }
    
    // remove an element or a diagram
    public void rmElement(String queryId, List<String> path, Map<String, String> queryMap) {
        try {
            ACLMessage msg = Messaging.rmElement(this, queryMap, path);
            msg.setConversationId(queryId);
            
            String diaName = path.get(0);
            msg.addReplyTo(Services.getClock(this, diaName));
            
            this.send(msg);
            addBehaviour(new ReceiveBhv(this, queryId));
        }
        catch (RuntimeException re) {
            // do not raise an error if the diagram to remove does not exists
            if (path.size() == 1) {
                Map<String, String> map = new HashMap<>();
                map.put(Messaging.STATUS, Errors.OK);
                map.put(Messaging.TYPE, Method.DELETE.toString());
                map.put(Messaging.PATH, JSON.serializeStringList(path));
                RestServer.returnQueue.put(queryId, JSON.serializeStringMap(map));
            }
            else {
                throw re;
            }
        }
    }
    
    // rm a list of properties from an element
    public void rmProperties(String queryId, List<String> path, List<String> propertiesList, Map<String, String> queryMap) {
        ACLMessage msg = Messaging.rmProperties(this, queryMap, path, propertiesList);
        msg.setConversationId(queryId);
        
        String diaName = path.get(0);
        msg.addReplyTo(Services.getClock(this, diaName));
        
        this.send(msg);
        addBehaviour(new ReceiveBhv(this, queryId));
    }
    
    // change/add properties of an element
    public void chProperties(String queryId, List<String> path, Map<String, String> propertyMap, Map<String, String> queryMap) {
        ACLMessage msg = Messaging.chProperties(this, queryMap, path, propertyMap);
        msg.setConversationId(queryId);
        
        String diaName = path.get(0);
        msg.addReplyTo(Services.getClock(this, diaName));
        
        this.send(msg);
        addBehaviour(new ReceiveBhv(this, queryId));
    }
    
    // retrieve the complete element description
    public void getElementDescription(String queryId, List<String> path, Map<String, String> queryMap) {
        ACLMessage msg = Messaging.getElementDescription(this, queryMap, path);
        msg.setConversationId(queryId);
        
        String diaName = path.get(0);
        msg.addReplyTo(Services.getClock(this, diaName));
        
        this.send(msg);
        addBehaviour(new ReceiveBhv(this, queryId));
    }
}
