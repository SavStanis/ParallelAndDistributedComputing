package com.savstanis.pdc.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import com.savstanis.pdc.messages.CallRequest;
import com.savstanis.pdc.messages.CallRequestAccepted;
import com.savstanis.pdc.messages.EndCallRequest;
import com.savstanis.pdc.messages.WaitForFreeOperator;
import com.savstanis.pdc.models.Client;

import java.util.Random;

public class ClientActor extends AbstractActor {
    private Client client;
    private ActorRef callCenter;
    private String clientLogBeginning;

    public ClientActor(Client client, ActorRef callCenter) {
        this.client = client;
        this.callCenter = callCenter;
        this.clientLogBeginning = "[Caller-" + client.getId() + "]:\t\t";

        callCenter.tell(new CallRequest(CallRequest.CallType.FIRST_CALL, client), this.getSelf());
    }

    public static Props props(Client client, ActorRef callCenter) {
        return Props.create(ClientActor.class, client, callCenter);
    }

    @Override
    public Receive createReceive() {
        return new ReceiveBuilder()
                .match(CallRequestAccepted.class, this::handleCallRequestAccepted)
                .match(WaitForFreeOperator.class, this::handleWaitForFreeOperator)
                .build();
    }

    private void handleWaitForFreeOperator(WaitForFreeOperator waitForFreeOperator) {
        System.out.println(clientLogBeginning + "Waiting for a free Operator");
    }

    private void handleCallRequestAccepted(CallRequestAccepted callRequestAccepted) throws InterruptedException {
        ActorRef sender = getSender();

        Random random = new Random();
        int callDuration = random.nextInt(1000, 20000);

        System.out.println(clientLogBeginning + "Connected to Operator-" + callRequestAccepted.getOperator().getId()
                + ". Expected call duration: " + callDuration + " ms");
        Thread.sleep(callDuration);

        sender.tell(new EndCallRequest(client, callRequestAccepted.getOperator()), this.getSelf());

        if (random.nextDouble() > 0.8) {
            System.out.println(clientLogBeginning + "Call ended successfully. No need to repeat the call");
        } else {
            int callInterval = random.nextInt(10000, 20000);
            System.out.println(clientLogBeginning + "Call ended. Need to repeat the call after " + callInterval + " ms");
            Thread.sleep(callInterval);
            sender.tell(new CallRequest(CallRequest.CallType.REPEATED_CALL, client), this.getSelf());
        }
    }
}
