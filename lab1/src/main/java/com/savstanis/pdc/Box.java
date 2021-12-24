package com.savstanis.pdc;

import com.savstanis.pdc.mutex.CasMutex;

public class Box extends CasMutex {
    private String message;


    public void putMessage(String newMessage) {
        lock();
        while (message != null) {
            customWait();
        }

        message = newMessage;
        customNotifyAll();
        unlock();
    }

    public String getMessage() {
        lock();
        while (message == null) {
            customWait();
        }
        String retrievedMessage = message;
        message = null;

        customNotifyAll();
        unlock();

        return retrievedMessage;
    }
}
