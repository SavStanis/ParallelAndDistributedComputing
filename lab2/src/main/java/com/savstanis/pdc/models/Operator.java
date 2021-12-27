package com.savstanis.pdc.models;

public class Operator {
    private final int id;
    private boolean isBusy;
    private Client client;

    public Operator(int id, boolean isBusy, Client client) {
        this.id = id;
        this.isBusy = isBusy;
        this.client = client;
    }

    public boolean isBusy() {
        return isBusy;
    }

    public void setBusy(boolean busy) {
        isBusy = busy;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public int getId() {
        return id;
    }
}
