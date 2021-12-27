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
import com.savstanis.pdc.models.Operator;
import com.savstanis.pdc.models.WaitingClient;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class CallCenterActor extends AbstractActor {
    private final List<Operator> operators;
    private final Queue<WaitingClient> waitingClients;

    private static final String CALL_CENTER_LOG_BEGINNING = "[Call Center]:\t";

    public CallCenterActor(List<Operator> operators) {
        this.operators = operators;
        this.waitingClients = new LinkedList<>();
    }

    public static Props props(Integer operatorsAmount) {
        return Props.create(CallCenterActor.class, () -> {
            List<Operator> operators = new ArrayList<>();
            for (int i = 0; i < operatorsAmount; i++) {
                operators.add(new Operator(i, false, null));
            }

            return new CallCenterActor(operators);
        });
    }

    @Override
    public Receive createReceive() {
        return new ReceiveBuilder()
                .match(CallRequest.class, this::handleCallRequest)
                .match(EndCallRequest.class, this::handEndCall)
                .build();
    }

    private void handEndCall(EndCallRequest endCallRequest) {
        System.out.println(CALL_CENTER_LOG_BEGINNING + "Caller-" + endCallRequest.getClient().getId()
                + " ended call with Operator-" + endCallRequest.getOperator().getId());
        Operator operator = operators.stream().filter(o -> endCallRequest.getOperator().getId() == o.getId()).findFirst().orElse(null);
        if (operator == null) {
            return;
        }


        WaitingClient waitingClient = waitingClients.poll();
        if (waitingClient == null) {
            operator.setBusy(false);
            operator.setClient(null);
            return;
        }

        operator.setClient(waitingClient.getClient());
        System.out.println(CALL_CENTER_LOG_BEGINNING + "Caller-" + waitingClient.getClient().getId()
                + " request was accepted. Connecting to Operator-" + operator.getId());
        waitingClient.getClientActor().tell(new CallRequestAccepted(operator), this.getSelf());
    }

    private void handleCallRequest(CallRequest callRequest) {
        ActorRef caller = getSender();

        Operator freeOperator = findFreeOperator();
        String callType = callRequest.getCallType() == CallRequest.CallType.REPEATED_CALL ? "repeated call" : "call";
        System.out.println(CALL_CENTER_LOG_BEGINNING + "Caller-" + callRequest.getClient().getId() + " requested a " +  callType);

        if (freeOperator == null) {
            System.out.println(CALL_CENTER_LOG_BEGINNING + "Caller-" + callRequest.getClient().getId() + " should wait for a free operator");
            waitingClients.add(new WaitingClient(callRequest.getClient(), caller));
            caller.tell(new WaitForFreeOperator(callRequest.getClient()), this.getSelf());
        } else {
            System.out.println(CALL_CENTER_LOG_BEGINNING + "Caller-" + callRequest.getClient().getId()
                    + " request was accepted. Connecting to Operator-" + freeOperator.getId());
            freeOperator.setBusy(true);
            freeOperator.setClient(callRequest.getClient());
            caller.tell(new CallRequestAccepted(freeOperator), this.getSelf());
        }
    }

    private Operator findFreeOperator() {
        return operators.stream()
                .filter(o -> !o.isBusy())
                .findFirst()
                .orElse(null);
    }
}
