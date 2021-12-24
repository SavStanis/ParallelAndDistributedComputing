package com.savstanis.pdc.mutex;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CasMutex {
    private final AtomicReference<Thread> workingThread;
    private final AtomicReference<Thread> notifiedThread;
    private final AtomicReference<Thread> notifyingAllThread;
    private final AtomicInteger waitingThreadsNumber;
    private final ConcurrentHashMap<Thread, Integer> waitingThreads;

    public CasMutex() {
        workingThread = new AtomicReference<>();
        notifiedThread = new AtomicReference<>();
        notifyingAllThread = new AtomicReference<>();
        waitingThreadsNumber = new AtomicInteger();
        waitingThreads = new ConcurrentHashMap<>();
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
        waitingThreadsNumber.incrementAndGet();
        waitingThreads.put(Thread.currentThread(), 0);
        unlock();

        while (true) {
            if (notifiedThread.compareAndSet(Thread.currentThread(), null)) {
                break;
            }

            Thread notifyingAllThreadVal = notifyingAllThread.get();
            if (notifyingAllThreadVal != null && notifyingAllThread.get() != Thread.currentThread()) {
                break;
            }

            Thread.yield();
        }

        waitingThreadsNumber.decrementAndGet();
        waitingThreads.remove(Thread.currentThread());
        lock();
    }

    protected void customNotify() {
        waitingThreads.keySet()
                .stream()
                .findAny()
                .ifPresent(waitingThread -> notifiedThread.compareAndSet(null, waitingThread));
    }

    protected void customNotifyAll() {
        notifyingAllThread.compareAndSet(null, Thread.currentThread());

        while (waitingThreadsNumber.get() != 0) {
            Thread.yield();
        }

        notifyingAllThread.compareAndSet(Thread.currentThread(), null);
    }
}
