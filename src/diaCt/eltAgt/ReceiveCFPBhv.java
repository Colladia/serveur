package diaCt.eltAgt;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ReceiveCFPBhv extends CyclicBehaviour{
    private EltAgt parentAgt;

    public ReceiveCFPBhv(EltAgt parentAgt) {
        this.parentAgt = parentAgt;
    }

    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
        ACLMessage message = parentAgt.receive(mt);
        if (message != null) {
            parentAgt.addBehaviour(new ProposeBhv(parentAgt, message));
        }
        else {
            block();
        }
    }
}
