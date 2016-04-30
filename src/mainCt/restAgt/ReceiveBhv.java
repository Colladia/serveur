package mainCt.restAgt;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.restlet.data.Method;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveBhv extends Behaviour{
    private RestAgt parentAgt;
    private String queryId;
    private boolean isDone = false;

    public ReceiveBhv(RestAgt parentAgt, String queryId) {
        this.parentAgt = parentAgt;
        this.queryId = queryId;
    }
    
    // wait a query response from a DiaAgt
    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchConversationId(queryId);
        ACLMessage message = parentAgt.receive(mt);
        if (message != null) {
            RestServer.returnQueue.put(queryId, message.getContent());
            isDone = true;
        }
        else{
            block();
        }
    }

    @Override
    public boolean done() {
        return isDone;
    }
}
