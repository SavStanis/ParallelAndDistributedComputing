package com.savstanis.pdc.messages;

import com.savstanis.pdc.models.Client;

public class WaitForFreeOperator {
    private final Client client;

    public WaitForFreeOperator(Client client) {
        this.client = client;
    }
}
