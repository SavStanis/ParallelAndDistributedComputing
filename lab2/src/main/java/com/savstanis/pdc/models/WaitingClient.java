package com.savstanis.pdc.models;

import akka.actor.ActorRef;

public class WaitingClient {
    private ActorRef clientActor;
    private Client client;

    public WaitingClient(Client client, ActorRef clientActor) {
        this.clientActor = clientActor;
        this.client = client;
    }

    public ActorRef getClientActor() {
        return clientActor;
    }

    public Client getClient() {
        return client;
    }
}
