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
import java.io.File;

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
        System.out.println("Save tick !");
        
        List<AID> diagramList = Services.getDiagramList(parentAgt);
        List<String> diagramNameList = new ArrayList<>();
        for (AID i : diagramList) {
            diagramNameList.add(i.getLocalName().substring("DiaAgt-".length()));
        }
        
        // remove file of related to non-existing diagram
        File saveDir = new File(parentAgt.SAVE_DIR);
        File[] fileList = saveDir.listFiles();
        for (File f : fileList) {
            // retrieve diagram name (remove extension)
            String diaName = f.getName();
            int pos = diaName.lastIndexOf(".");
            if (pos > 0) {
                diaName = diaName.substring(0, pos);
            }
            
            if (diagramNameList.indexOf(diaName) == -1) {
                f.delete();
            }
        }
        
        // send message to get description of every diagram
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        
        for (AID i : diagramList) {
            message.addReceiver(i);
        }
        
        Map<String, String> map = new HashMap<>();
        map.put(Messaging.TYPE, Method.GET.toString());
        map.put(Messaging.PATH, "[\"\"]");
        message.setContent(JSON.serializeStringMap(map));
        
        parentAgt.send(message);
    }
}
