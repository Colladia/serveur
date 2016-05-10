package mainCt;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class MainCt {
    private static String MAIN_CONF = "mainCt/MainCt.conf";
    private static String DIA_CONF = "diaCt/DiaCt.conf";

    public static void main(String[] args) {
        Runtime rt = Runtime.instance();
        try{
            // create main container
            Profile mainProfile = new ProfileImpl(MAIN_CONF);
            AgentContainer mainContainer = rt.createMainContainer(mainProfile);
            
            // create diagram container
            Profile diaProfile = new ProfileImpl(DIA_CONF);
            AgentContainer diaContainer = rt.createAgentContainer(diaProfile);

            // create RestAgt
            AgentController agentCc = mainContainer.createNewAgent("RestAgt", "mainCt.restAgt.RestAgt", new Object[]{diaContainer});
            agentCc.start();
            
            // create SaveAgt
            agentCc = mainContainer.createNewAgent("SaveAgt", "mainCt.saveAgt.SaveAgt", new Object[]{diaContainer});
            agentCc.start();
        }
        catch(Exception e){
            System.err.println(e);
        }
    }
}
