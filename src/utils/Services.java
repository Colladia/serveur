package utils;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import utils.Errors;

public class Services {
    public static String HISTORY = "history";
    public static String DIAGRAM = "diagram";
    public static String CLOCK = "clock";
    public static String REST = "REST";
    
    private static String DIA_CONF = "diaCt/DiaCt.conf";
    private static AgentContainer diaContainer = null;
    
    // deregister agent
    public static void deregisterService(Agent agent) {
        try {
            DFService.deregister(agent);
        } catch (FIPAException e) {
            System.err.println(e);
        }
    }
    
    // register an agent description to the DF
    public static void registerService(Agent agent, String typeService, String nameSpecificService) {
        DFAgentDescription dfad = new DFAgentDescription();
        dfad.setName(agent.getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType(typeService);
        sd.setName(nameSpecificService);

        dfad.addServices(sd);

        try {
            DFService.register(agent, dfad);
        } catch (FIPAException e) {
            System.err.println(e);
        }
    }
    
    // get a list of the agents matching the agent description
    public static AID[] getAgentsByService(Agent agent, String typeService, String nameSpecificService) {
        DFAgentDescription[] serviceList = null;
        
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(typeService);
        if (nameSpecificService != null) {
            sd.setName(nameSpecificService);
        }
        template.addServices(sd);
        
        try {
            serviceList = DFService.search(agent, template);
        } catch(FIPAException e) {
            System.err.println(e);
        }
        
        AID[] AIDList = new AID[serviceList.length];
        for (int i=0; i<serviceList.length; i++) {
            AIDList[i] = serviceList[i].getName();
        }
        
        return AIDList;
    }
    
    // create the diagram container if it does not already exists
    public static void createDiaContainer() {
        if (diaContainer == null) {
            try {
                // create diagram container
                Runtime rt = Runtime.instance();
                Profile diaProfile = new ProfileImpl(DIA_CONF);
                diaContainer = rt.createAgentContainer(diaProfile);
            }
            catch(Exception e){
                System.err.println(e);
            }
        }
    }
    
    // create a new diagram agent
    public static void addNewDiagram(Agent agent, String diaName) {
        createDiaContainer();
        boolean alreadyExists = false;
        try {
            getDiagram(agent, diaName);
            alreadyExists = true;
        }
        catch(RuntimeException re) {
            try {
                // create DiaAgt if it does not exists yet
                AgentController agentCc = diaContainer.createNewAgent("DiaAgt-"+diaName, "diaCt.eltAgt.EltAgt", null);
                agentCc.start();
                
                agentCc = diaContainer.createNewAgent("ClockAgt-"+diaName, "diaCt.clockAgt.ClockAgt", null);
                agentCc.start();
                
                agentCc = diaContainer.createNewAgent("HistAgt-"+diaName, "diaCt.histAgt.HistAgt", null);
                agentCc.start();
            }
            catch (Exception e) {
                Errors.throwKO("Unable to create diagram '"+diaName+"'");
            }
        }
        
        if (alreadyExists) {
            Errors.throwKO("Diagram '"+diaName+"' already exists");
        }
    }
    
    // create new diagram element
    public static AID addNewElement(Agent parentAgt, List<String> path, Map<String, String> propertyMap) {
        createDiaContainer();
        String eltName = "EltAgt-"+String.join("/", path);
        try {
            AgentController agentCc = diaContainer.createNewAgent(eltName, "diaCt.eltAgt.EltAgt", new Object[]{parentAgt.getAID(), path, propertyMap});
            agentCc.start();
            return new AID(eltName, AID.ISLOCALNAME);
        }
        catch(Exception e){
            Errors.throwKO("Unable to create element '"+eltName+"'");
            return null;
        }
    }
    
    // return a list of the diagram names
    public static List<AID> getDiagramList(Agent agent) {
        AID[] AIDList = Services.getAgentsByService(agent, "Diagram", null);
        return Arrays.asList(AIDList);
    }
    
    // return a list of the diagram names
    public static List<String> getDiagramNameList(Agent agent) {
        List<AID> AIDList = getDiagramList(agent);
        List<String> r = new ArrayList<>();
        for (AID i : AIDList) {
            r.add(i.getLocalName().substring("DiaAgt-".length()));
        }
        return r;
    }
    
    // retrieve the AID of a diagram from its name or throw an error
    public static AID getDiagram(Agent agent, String diaName) {
        AID[] services = Services.getAgentsByService(agent, DIAGRAM, diaName);
        
        if (services.length <= 0) {
            Errors.throwKO("Diagram '"+diaName+"' does not exists");
        }

        return services[0];
    }
    
    // retrieve the AID of a diagram clock from the name of the diagram or throw an error
    public static AID getClock(Agent agent, String diaName) {
        AID[] services = Services.getAgentsByService(agent, CLOCK, diaName);
        
        if (services.length <= 0) {
            Errors.throwKO("Clock of diagram '"+diaName+"' does not exists");
        }

        return services[0];
    }
    
    // retrieve the AID of a diagram history from the name of the diagram or throw an error
    public static AID getHist(Agent agent, String diaName) {
        AID[] services = Services.getAgentsByService(agent, HISTORY, diaName);
        
        if (services.length <= 0) {
            Errors.throwKO("History of diagram '"+diaName+"' does not exists");
        }

        return services[0];
    }
}
