package com.savstanis.pdc;

import java.util.List;

public class MutexMain {
    private static final String MESSAGE = "Hello world!";

    public static void main(String[] args) {
        var box = new Box();

        List<String> messages = MESSAGE.chars()
                .mapToObj(c -> (char) c)
                .map(String::valueOf)
                .toList();

        Thread producer = new Thread(() -> {
            System.out.println("Producer initial message: " + MESSAGE);
            messages.forEach(box::putMessage);
        });

        Thread consumer = new Thread(() -> {

            StringBuilder messageBuilder = new StringBuilder();
            for (int i = 0; i < MESSAGE.length(); i++) {
                String m = box.getMessage();
                System.out.println("Consumer: got " + m);
                messageBuilder.append(m);
            }
            System.out.println("Consumed message: " + messageBuilder);
        });


        producer.start();
        consumer.start();
    }
}
