package mainCt;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class MainCt {
    public static String MAIN_CONF = "mainCt/MainCt.conf";

    public static void main(String[] args) {
        Runtime rt = Runtime.instance();
        try{
            // create main container
            Profile mainProfile = null;
            mainProfile = new ProfileImpl(MAIN_CONF);
            AgentContainer mainContainer = rt.createMainContainer(mainProfile);

            // create RestAgt
            AgentController agentCc = mainContainer.createNewAgent("RestAgt", "mainCt.restAgt.RestAgt", null);
            agentCc.start();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
