package com.savstanis.pdc.messages;

import com.savstanis.pdc.models.Client;
import com.savstanis.pdc.models.Operator;

public class EndCallRequest {
    private final Client client;
    private final Operator operator;

    public EndCallRequest(Client client, Operator operator) {
        this.client = client;
        this.operator = operator;
    }

    public Client getClient() {
        return client;
    }

    public Operator getOperator() {
        return operator;
    }
}
