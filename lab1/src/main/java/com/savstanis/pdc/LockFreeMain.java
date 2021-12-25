package com.savstanis.pdc;

import com.savstanis.pdc.algorithm.LockFreeLinkedList;
import com.savstanis.pdc.algorithm.LockFreeQueue;

public class LockFreeMain {
    public static void main(String[] args) {
        LockFreeLinkedList<Integer> lockFreeLinkedList = new LockFreeLinkedList<>();

        for (int i = 0; i < 100; i++) {
            lockFreeLinkedList.add(i);
        }
        System.out.println();

        for (int i = 0; i < 100; i++) {
            System.out.println(lockFreeLinkedList.contains(i));
            lockFreeLinkedList.remove(i);
            System.out.println(lockFreeLinkedList.contains(i));

        }
    }
}
