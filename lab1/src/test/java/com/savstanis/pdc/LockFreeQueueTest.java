package com.savstanis.pdc;

import com.savstanis.pdc.algorithm.LockFreeQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class LockFreeQueueTest {
    private static final int THREADS_NUMBER = 1000;
    private static final int ELEMENTS_PER_THREAD = 100;

    @Test
    void addFixedNumberOfElementsAndRetrieveThem_allElementsShouldBePutInQueueAndRetrievedSuccessfully() {
        LockFreeQueue<Integer> queue = new LockFreeQueue<>();

        addElementsInQueue(queue, THREADS_NUMBER, ELEMENTS_PER_THREAD);
        Assertions.assertEquals(THREADS_NUMBER * ELEMENTS_PER_THREAD, queue.size());
        retrieveElementsFromQueue(queue, THREADS_NUMBER, ELEMENTS_PER_THREAD);
        Assertions.assertEquals(0, queue.size());
    }

    private void addElementsInQueue(LockFreeQueue<Integer> queue, int threadsNumber, int elementsPerThread) {
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < threadsNumber; i++) {
            int threadId = i;

            threads.add(new Thread(() -> {
                for (int j = 0; j < elementsPerThread; j++) {
                    queue.push(threadId);
                }
            }));
        }

        threads.forEach(Thread::start);
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void retrieveElementsFromQueue(LockFreeQueue<Integer> queue, int threadsNumber, int elementsPerThread) {
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < threadsNumber; i++) {
            threads.add(new Thread(() -> {
                for (int j = 0; j < elementsPerThread; j++) {
                    queue.pop();
                }
            }));
        }

        threads.forEach(Thread::start);
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
