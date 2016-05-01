package mainCt.restAgt;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.restlet.data.Method;

import jade.core.Agent;
import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import utils.JSON;
import utils.Services;
import utils.Messaging;
import mainCt.restAgt.RestServer;

public class RestAgt extends Agent {
    private AgentContainer diaContainer = null;
    
    protected void setup() {
        // create diagram container
        Runtime rt = Runtime.instance();
        Profile diaProfile = null;
        diaProfile = new ProfileImpl("127.0.0.1", -1, null, false);
        diaProfile.setParameter(Profile.CONTAINER_NAME, "DiaCt");
        diaContainer = rt.createAgentContainer(diaProfile);
        
        RestServer.launchServer(this);
    }
    
    // retrieve the AID of a diagram from its name or throw an error
    public AID getDiagram(String diaName) {
        AID[] services = Services.getAgentsByService(this, "Diagram", diaName);
        
        if (services.length > 0) {
            return services[0];
        }
        else {
            throw new RuntimeException("Diagram '"+diaName+"' does not exists");
        }
    }
    
    // create a new diagram container and its agents
    public void addNewDiagram(String name) {
        try{
            // create DiaAgt
            AgentController agentCc = diaContainer.createNewAgent("DiaAgt-"+name, "diaCt.diaAgt.DiaAgt", null);
            agentCc.start();
        }
        catch(Exception e){
            throw new RuntimeException("Diagram '"+name+"' already exists");
        }
    }
    
    // create a new element and sets its propreties
    public void addNewElement(String queryId, List<String> path, String propertyMapSerialized) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        
        String diaName = path.get(0);
        path = path.subList(1, path.size());
        message.addReceiver(getDiagram(diaName));
        
        Map<String, String> map = new HashMap<>();
        map.put(Messaging.TYPE, Method.PUT.toString());
        map.put(Messaging.PATH, JSON.serializeStringList(path));
        map.put(Messaging.DESCRIPTION, propertyMapSerialized);
        
        message.setContent(JSON.serializeStringMap(map));
        message.setConversationId(queryId);
        this.send(message);
        
        addBehaviour(new ReceiveBhv(this, queryId));
    }
    
    // retrieve the complete element description
    public void getElementDescription(String queryId, List<String> path) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        
        String diaName = path.get(0);
        path = path.subList(1, path.size());
        message.addReceiver(getDiagram(diaName));
        
        Map<String, String> map = new HashMap<>();
        map.put(Messaging.TYPE, Method.GET.toString());
        map.put(Messaging.PATH, JSON.serializeStringList(path));
        //map.put(Messaging.PROPERTIES, propertyMapSerialized);
        
        message.setContent(JSON.serializeStringMap(map));
        message.setConversationId(queryId);
        this.send(message);
        
        addBehaviour(new ReceiveBhv(this, queryId));
    }
}
