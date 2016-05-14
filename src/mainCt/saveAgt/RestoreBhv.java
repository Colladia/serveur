package mainCt.saveAgt;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;

import org.restlet.data.Method;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

import utils.JSON;
import utils.Services;
import utils.Messaging;

public class RestoreBhv extends OneShotBehaviour{
    private SaveAgt parentAgt;

    public RestoreBhv(SaveAgt parentAgt) {
        this.parentAgt = parentAgt;
    }
    
    @Override
    public void action() {
        // retrieve the list of files in the save directory
        File saveDir = new File(parentAgt.SAVE_DIR);
        
        if (! saveDir.exists()) {
            // create directory if it does not exists
            saveDir.mkdir();
        }
        else {
            File[] fileList = saveDir.listFiles();
            for (File f : fileList) {
                if (f.isFile()) {
                    try {
                        List<String> lines = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
                        
                        // retrieve diagram name (remove extension)
                        String diaName = f.getName();
                        int pos = diaName.lastIndexOf(".");
                        if (pos > 0) {
                            diaName = diaName.substring(0, pos);
                        }
                        
                        // create the diagram
                        Services.addNewDiagram(parentAgt, parentAgt.diaContainer, diaName);
                        
                        // restore its elements
                        boolean done = false;
                        while (! done) {
                            try {
                                parentAgt.send(Messaging.restoreElements(parentAgt, null, diaName, lines.get(0)));
                                done = true;
                            }
                            catch (RuntimeException re) {}
                        }
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        // add save related behaviours
        parentAgt.addBehaviour(new ReceiveBhv(parentAgt));
        parentAgt.addBehaviour(new TickerBhv(parentAgt, parentAgt.DELAY));
    }
}
