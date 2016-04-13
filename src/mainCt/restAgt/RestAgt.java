package mainCt.restAgt;

import jade.core.Agent;
import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

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
    
    // create a new diagram container and its agents
    public void addNewDiagram(String name) {
        try{
            // create DiaAgt
            AgentController agentCc = diaContainer.createNewAgent("DiaAgt-"+name, "diaCt.diaAgt.DiaAgt", null);
            agentCc.start();
        }
        catch(jade.wrapper.StaleProxyException e){
            throw new RuntimeException("Diagram name already exists");
        }
    }
}
