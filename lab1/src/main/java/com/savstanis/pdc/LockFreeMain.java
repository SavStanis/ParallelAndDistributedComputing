package com.savstanis.pdc;

import com.savstanis.pdc.algorithm.LockFreeQueue;

public class LockFreeMain {
    public static void main(String[] args) {
        LockFreeQueue<Integer> queue = new LockFreeQueue<>();
        for (int i = 0; i < 100; i++) {
            queue.push(i);
        }
        System.out.println(queue.size());

        for (int i = 0; i < 100; i++) {
            System.out.println(queue.pop());
        }

        System.out.println(queue.size());
    }
}
