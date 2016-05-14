package utils;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import utils.Errors;

public class Services {
    public static String HISTORY = "history";
    public static String DIAGRAM = "diagram";
    public static String CLOCK = "clock";
    public static String REST = "REST";
    
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
    
    
    
    // create a new diagram agent
    public static void addNewDiagram(Agent agent, AgentContainer diaContainer, String diaName) {
        try {
            getDiagram(agent, diaName);
        }
        catch(RuntimeException re) {
            try {
                // create DiaAgt if it does not exists yet
                AgentController agentCc = diaContainer.createNewAgent("DiaAgt-"+diaName, "diaCt.diaAgt.DiaAgt", null);
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
