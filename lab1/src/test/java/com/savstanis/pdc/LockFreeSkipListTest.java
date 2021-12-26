package com.savstanis.pdc;

import com.savstanis.pdc.algorithm.LockFreeSkipList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class LockFreeSkipListTest {
    private static final int VALUES_TOTAL_AMOUNT = 2000;

    @Test
    void addAndRemoveValuesConcurrently_finalSkipListStateShouldMatchExpectedState() throws InterruptedException {
        LockFreeSkipList<Integer> lockFreeLinkedList = new LockFreeSkipList<>();

        List<Integer> initValues = new ArrayList<>();
        List<Integer> valuesToAddConcurrently = new ArrayList<>();

        for (int i = 0; i < VALUES_TOTAL_AMOUNT; i++) {
            if (i % 2 == 0) {
                initValues.add(i);
            } else {
                valuesToAddConcurrently.add(i);
            }
        }

        addValues(initValues, lockFreeLinkedList);

        Thread addingThread = new Thread(() -> addValues(valuesToAddConcurrently, lockFreeLinkedList));
        Thread removingThread = new Thread(() -> removeValues(initValues, lockFreeLinkedList));

        addingThread.start();
        removingThread.start();

        addingThread.join();
        removingThread.join();

        Assertions.assertTrue(containsAllValues(valuesToAddConcurrently, lockFreeLinkedList));
        Assertions.assertTrue(doNotContainAnyValue(initValues, lockFreeLinkedList));
    }

    private <T extends Comparable<T>> void  addValues(List<T> values, LockFreeSkipList<T> lockFreeLinkedList) {
        values.forEach(lockFreeLinkedList::add);
    }

    private <T extends Comparable<T>> void removeValues(List<T> values, LockFreeSkipList<T> lockFreeLinkedList) {
        values.forEach(lockFreeLinkedList::remove);
    }

    private <T extends Comparable<T>> boolean containsAllValues(List<T> values, LockFreeSkipList<T> lockFreeLinkedList) {
        for (T value : values) {
            if (!lockFreeLinkedList.contains(value)) {
                return false;
            }
        }

        return true;
    }

    private <T extends Comparable<T>> boolean doNotContainAnyValue(List<T> values, LockFreeSkipList<T> lockFreeLinkedList) {
        for (T value : values) {
            if (lockFreeLinkedList.contains(value)) {
                return false;
            }
        }

        return true;
    }
}
