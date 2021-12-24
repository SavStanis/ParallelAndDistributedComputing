package com.savstanis.pdc.mutex;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CasMutex {
    private final AtomicReference<Thread> workingThread;
    private final AtomicReference<Thread> notifyingThread;
    private final AtomicReference<Thread> notifyingAllThread;
    private final AtomicInteger waitingThreads;

    public CasMutex() {
        workingThread = new AtomicReference<>();
        notifyingThread = new AtomicReference<>();
        notifyingAllThread = new AtomicReference<>();
        waitingThreads = new AtomicInteger();
    }

    protected void lock() {
        while (!workingThread.compareAndSet(null, Thread.currentThread())) {
            Thread.yield();
        }
    }

    protected void unlock() {
        workingThread.compareAndSet(Thread.currentThread(), null);
    }

    protected void customWait() {
        unlock();
        waitingThreads.incrementAndGet();

        while (!notifyingThread.compareAndSet(workingThread.get(), null) && !(notifyingAllThread.get() != null && notifyingThread.get() != Thread.currentThread())) {
            Thread.yield();
        }

        waitingThreads.decrementAndGet();
        lock();
    }

    protected void customNotify() {
        notifyingThread.compareAndSet(null, Thread.currentThread());
    }

    protected void customNotifyAll() {
        notifyingAllThread.compareAndSet(null, Thread.currentThread());

        while (waitingThreads.get() != 0) {
            Thread.yield();
        }

        notifyingAllThread.compareAndSet(Thread.currentThread(), null);
    }
}
