package com.savstanis.pdc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class MutexTest {
    private static final String MESSAGE = "Hello world!";

    @Test
    void sendMessageFromProducerThreadViaBoxByLetters_messageShouldBeConsumedAndRebuiltByConsumerThread() throws InterruptedException {
        Box box = new Box();

        List<String> messages = MESSAGE.chars()
                .mapToObj(c -> (char) c)
                .map(String::valueOf)
                .toList();

        Thread producer = new Thread(() -> messages.forEach(box::putMessage));

        Thread consumer = new Thread(() -> {

            StringBuilder messageBuilder = new StringBuilder();
            for (int i = 0; i < MESSAGE.length(); i++) {
                String m = box.getMessage();
                messageBuilder.append(m);
            }
            Assertions.assertEquals(messageBuilder.toString(), MESSAGE);
//            System.out.println(MESSAGE);
//            System.out.println(messageBuilder.toString());
        });

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
    }
}
