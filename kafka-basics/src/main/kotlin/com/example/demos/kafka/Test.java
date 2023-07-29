package com.example.demos.kafka;

public class Test {
    public void test() {
        Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            //log.info("Detected a shutdown, let's exit by calling consumer.wakeup()...")
            //consumer.wakeup()

            // join the main thread to allow the execution of the code in the main thread
            try {
                mainThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }
}
