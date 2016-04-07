package mainCt.restAgt;

import jade.core.Agent;
import jade.core.AID;

import mainCt.restAgt.RestServer;

public class RestAgt extends Agent {
    protected void setup() {
        RestServer.launchServer(this);
    }
}
