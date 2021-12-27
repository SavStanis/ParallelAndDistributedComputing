package com.savstanis.pdc.messages;

import com.savstanis.pdc.models.Client;

public class CallRequest {
    public enum CallType {
        FIRST_CALL, REPEATED_CALL
    }

    private final CallType callType;
    private final Client client;

    public CallRequest(CallType callType, Client client) {
        this.callType = callType;
        this.client = client;
    }

    public CallType getCallType() {
        return callType;
    }

    public Client getClient() {
        return client;
    }
}
