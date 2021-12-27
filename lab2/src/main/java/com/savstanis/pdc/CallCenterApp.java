package com.savstanis.pdc;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.savstanis.pdc.actors.CallCenterActor;
import com.savstanis.pdc.actors.ClientActor;
import com.savstanis.pdc.models.Client;

import java.util.Arrays;

public class CallCenterApp {
    private static final int CALLERS_NUMBER = 2;

    public static void main(String[] args) throws InterruptedException {
        ActorSystem actorSystem = ActorSystem.create("callCenter");
        ActorRef callCenter = actorSystem.actorOf(CallCenterActor.props(1), "callCenter");


        Thread[] threads = new Thread[CALLERS_NUMBER];
        for (int i = 0; i < CALLERS_NUMBER; i++) {
            int finalI = i;
            threads[i] = new Thread(() -> {
                actorSystem.actorOf(ClientActor.props(new Client(finalI), callCenter), "client-" + finalI);
            });

            threads[i].start();
        }
    }
}
