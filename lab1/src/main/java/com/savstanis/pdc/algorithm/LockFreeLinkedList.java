package com.savstanis.pdc.algorithm;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class LockFreeLinkedList<T extends Comparable<T>> {
    private final Node<T> head;
    private final Node<T> tail;

    public LockFreeLinkedList() {
        tail = new Node<>(null);
        head = new Node<>(null);

        head.next.set(tail, false);
    }

    public boolean add(T value) {
        Node<T> newNode = new Node<>(value);

        while (true) {
            ListPosition<T> listPosition = find(value);

            Node<T> leftNode = listPosition.leftNode;
            Node<T> currNode = listPosition.currNode;

            if (currNode.value == value) {
                return false;
            } else {
                newNode.next.set(currNode, false);
                if (leftNode.next.compareAndSet(currNode, newNode, false, false)) {
                    return true;
                }
            }
        }
    }

    public boolean remove(T key) {
        while (true) {
            ListPosition<T> listPos = find(key);

            Node<T> leftNode = listPos.leftNode;
            Node<T> currNode = listPos.currNode;

            if (currNode.value != key) {
                return false;
            }

            Node<T> rightNode = currNode.next.getReference();

            if (!currNode.next.compareAndSet(rightNode, rightNode, false, true)) {
                continue;
            }

            leftNode.next.compareAndSet(currNode, rightNode, false, false);
            return true;
        }
    }

    public ListPosition<T> find(T value) {
        Node<T> leftNode;
        Node<T> currNode;
        Node<T> rightNode;

        boolean[] marked = {false};

        if (head.next.getReference() == tail) {
            return new ListPosition<>(head, tail);
        }

        retry:
        while (true) {
            leftNode = head;
            currNode = leftNode.next.getReference();
            while (true) {
                rightNode = currNode.next.get(marked);
                while (marked[0]) {
                    if (!leftNode.next.compareAndSet(currNode, rightNode, false, false)) {
                        continue retry;
                    }
                    currNode = rightNode;
                    rightNode = currNode.next.get(marked);
                }

                if (currNode == tail || value.compareTo(currNode.value) <= 0) {
                    return new ListPosition<>(leftNode, currNode);
                }
                leftNode = currNode;
                currNode = rightNode;
            }
        }
    }

    public boolean contains(T value) {
        boolean[] marked = {false};
        Node<T> currNode = head.next.getReference();
        currNode.next.get(marked);

        while (currNode != tail && value.compareTo(currNode.value) > 0) {
            currNode = currNode.next.getReference();
            currNode.next.get(marked);
        }
        return (currNode.value == value && !marked[0]);
    }

    private static class Node<T> {
        private final T value;
        private final AtomicMarkableReference<Node<T>> next;

        public Node(T value) {
            this.value = value;
            this.next = new AtomicMarkableReference<>(null, false);
        }
    }

    private static class ListPosition<T> {
        public Node<T> leftNode;
        public Node<T> currNode;

        public ListPosition(Node<T> leftNode, Node<T> currNode) {
            this.leftNode = leftNode;
            this.currNode = currNode;
        }
    }
}
