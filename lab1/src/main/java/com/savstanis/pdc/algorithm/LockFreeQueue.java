package com.savstanis.pdc.algorithm;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

public class LockFreeQueue<T> {

    private final AtomicReference<Node<T>> headRef;
    private final AtomicReference<Node<T>> tailRef;

    public LockFreeQueue() {
        Node<T> dummyNode = new Node<>(null, new AtomicReference<>());

        headRef = new AtomicReference<>(dummyNode);
        tailRef = new AtomicReference<>(dummyNode);
    }

    public void push(T value) {
        Node<T> newTail = new Node<>(value, new AtomicReference<>());

        while (true) {
            Node<T> tail = tailRef.get();

            if (tail.nextRef.compareAndSet(null, newTail)) {
                tailRef.compareAndSet(tail, newTail);
                return;
            } else {
                tailRef.compareAndSet(tail, tail.nextRef.get());
            }
        }
    }

    public T pop() {
        while (true) {
            Node<T> head = headRef.get();
            Node<T> tail = tailRef.get();
            Node<T> nextHead = head.nextRef.get();

            if (head == tail) {
                if (nextHead == null) {
                    throw new NoSuchElementException();
                } else {
                    tailRef.compareAndSet(tail, nextHead);
                }
            } else {
                T res = nextHead.value;
                if (headRef.compareAndSet(head, nextHead)) {
                    return res;
                }
            }
        }
    }

    public int size() {
        int counter = 0;
        Node<T> curr = headRef.get();

        while (curr.nextRef.get() != null) {
            counter++;
            curr = curr.nextRef.get();
        }

        return counter;
    }

    private static class Node<T> {
        private final T value;
        private final AtomicReference<Node<T>> nextRef;

        public Node(T value, AtomicReference<Node<T>> nextRef) {
            this.value = value;
            this.nextRef = nextRef;
        }
    }
}
