package com.savstanis.pdc.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class LockFreeSkipList<T extends Comparable<T>> {
    private static final int MAX_LEVEL = 32;
    private static final int MIN_LEVEL = 0;

    private final Node<T> head;
    private final Node<T> tail;

    public LockFreeSkipList() {
        this.head = new Node<>();
        this.tail = new Node<>();

        init();
    }

    private void init() {
        for (int i = 0; i < MAX_LEVEL; i++) {
            head.next[i] = new AtomicMarkableReference<>(tail, false);
        }
    }

    public boolean add(T value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        int newTowerLevel = generateTowerLevel();

        List<Node<T>> prevTower = initTower();
        List<Node<T>> nextTower = initTower();

        while (true) {
            if (find(value, prevTower, nextTower)) {
                return false;
            }

            Node<T> newNode = new Node<>(value, newTowerLevel);
            connectNewNodeToNext(newNode, newTowerLevel, nextTower);

            Node<T> prevBottom = prevTower.get(MIN_LEVEL);
            Node<T> nextBottom = nextTower.get(MIN_LEVEL);

            // try again if we couldn't add the node at the bottom level
            if (!prevBottom.next[MIN_LEVEL].compareAndSet(nextBottom, newNode, false, false)) {
                continue;
            }

            connectTowerToNextTower(newNode, newTowerLevel, prevTower, nextTower);

            return true;
        }
    }

    private void connectNewNodeToNext(Node<T> newNode, int newTowerLevel, List<Node<T>> nextTower) {
        for (int level = MIN_LEVEL; level <= newTowerLevel; level++) {
            Node<T> next = nextTower.get(level);
            newNode.next[level].set(next, false);
        }
    }

    private void connectTowerToNextTower(Node<T> newNode, int newTowerLevel,
                                         List<Node<T>> prevTower, List<Node<T>> nextTower) {
        for (int level = MIN_LEVEL + 1; level <= newTowerLevel; level++) {
            while (true) {
                Node<T> prevBottom = prevTower.get(level);
                Node<T> nextBottom = nextTower.get(level);
                if (prevBottom.next[level].compareAndSet(nextBottom, newNode, false, false)) {
                    break;
                }

                find(newNode.value, prevTower, nextTower);
            }
        }
    }

    private List<Node<T>> initTower() {
        List<Node<T>> tower = new ArrayList<>(MAX_LEVEL);
        for (int i = 0; i < MAX_LEVEL; i++) {
            tower.add(null);
        }

        return tower;
    }

    public boolean remove(T value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        Node<T> next;

        List<Node<T>> prevTower = initTower();
        List<Node<T>> nextTower = initTower();

        if (!find(value, prevTower, nextTower)) {
            return false;
        }

        Node<T> nodeToRemove = nextTower.get(MIN_LEVEL);
        markTowerForRemoval(nodeToRemove);

        boolean[] marked = {false};

        next = nodeToRemove.next[MIN_LEVEL].get(marked);
        while (true) {
            boolean isMarked = nodeToRemove.next[MIN_LEVEL].compareAndSet(next, next, false, true);
            next = nextTower.get(MIN_LEVEL).next[MIN_LEVEL].get(marked);
            if (isMarked) {
                find(value, prevTower, nextTower);
                return true;
            } else if (marked[0]) {
                return false;
            }
        }
    }

    private void markTowerForRemoval(Node<T> nodeToRemove) {
        boolean[] marked = {false};

        for (int level = nodeToRemove.topLevel; level >= MIN_LEVEL + 1; level--) {
            Node<T> next = nodeToRemove.next[level].get(marked);
            while (!marked[0]) {
                nodeToRemove.next[level].compareAndSet(next, next, false, true);
                next = nodeToRemove.next[level].get(marked);
            }
        }
    }


    protected boolean find(T value, List<Node<T>> prevTower, List<Node<T>> nextTower) {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        boolean[] marked = {false};

        Node<T> prevNode;
        Node<T> currNode = null;
        Node<T> nextNode;

        retry:
        while (true) {
            prevNode = head;

            for (int level = MAX_LEVEL - 1; level >= MIN_LEVEL; level--) {
                currNode = prevNode.next[level].getReference();

                while (true) {
                    nextNode = currNode.next[level].get(marked);

                    // if node is marked then try to remove it
                    while (marked[0]) {
                        boolean isSuccessfullyRemoved = prevNode.next[level].compareAndSet(currNode, nextNode, false, false);
                        if (!isSuccessfullyRemoved) {
                            continue retry;
                        }

                        currNode = prevNode.next[level].getReference();
                        nextNode = currNode.next[level].get(marked);
                    }

                    if (currNode.value != null && currNode.value.compareTo(value) < 0) {
                        prevNode = currNode;
                        currNode = nextNode;
                    } else {
                        break;
                    }
                }

                prevTower.set(level, prevNode);
                nextTower.set(level, currNode);
            }

            return currNode.value != null && currNode.value.compareTo(value) == 0;
        }
    }

    public boolean contains(T value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        boolean[] marked = {false};
        Node<T> prevNode = head;
        Node<T> currNode = null;
        Node<T> nextNode;

        for (int level = MAX_LEVEL - 1; level >= MIN_LEVEL; level--) {
            currNode = prevNode.next[level].getReference();

            while (true) {
                nextNode = currNode.next[level].get(marked);
                while (marked[0]) {
                    currNode = currNode.next[level].getReference();
                    nextNode = currNode.next[level].get(marked);
                }

                if (currNode.value == null || currNode.value.compareTo(value) >= 0) {
                    break;
                }

                prevNode = currNode;
                currNode = nextNode;
            }
        }

        return currNode.value != null && currNode.value.compareTo(value) == 0;
    }

    private int generateTowerLevel() {
        var rand = new Random();
        int i = 0;
        for (; i < MAX_LEVEL; i++) {
            if (rand.nextInt() % 2 == 0) {
                break;
            }
        }

        return i + 1;
    }

    private static final class Node<T extends Comparable<T>> {
        public final T value;
        public final AtomicMarkableReference<Node<T>>[] next;
        private final int topLevel;

        public Node() {
            this.value = null;
            int capacity = MAX_LEVEL;
            this.next = (AtomicMarkableReference<Node<T>>[]) new AtomicMarkableReference[capacity];
            for (int i = 0; i < capacity; i++) {
                this.next[i] = new AtomicMarkableReference<>(null, false);
            }
            this.topLevel = MAX_LEVEL;
        }

        public Node(T value, int height) {
            this.value = value;
            this.next = (AtomicMarkableReference<Node<T>>[]) new AtomicMarkableReference[height + 1];
            for (int i = 0; i < height + 1; i++) {
                this.next[i] = new AtomicMarkableReference<>(null, false);
            }

            this.topLevel = height;
        }
    }
}