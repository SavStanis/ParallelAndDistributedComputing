package com.savstanis.pdc.messages;

import com.savstanis.pdc.models.Operator;

public class CallRequestAccepted {
    private final Operator operator;

    public CallRequestAccepted(Operator operator) {
        this.operator = operator;
    }

    public Operator getOperator() {
        return operator;
    }
}
