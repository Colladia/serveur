package utils;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Services {
    
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
        AID[] services = Services.getAgentsByService(agent, "Diagram", diaName);
        
        if (services.length <= 0) {
            Errors.throwKO("Diagram '"+diaName+"' does not exists");
        }

        return services[0];
    }
}
