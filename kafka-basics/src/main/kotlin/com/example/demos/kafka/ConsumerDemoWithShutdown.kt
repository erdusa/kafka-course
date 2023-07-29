package com.example.demos.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.Properties

fun main() {
    ConsumerDemoWithShutdown.main()
}

object ConsumerDemoWithShutdown {
    private val log = LoggerFactory.getLogger(ConsumerDemoWithShutdown::class.java.simpleName)
    fun main() {
        val bootstrapServer = "127.0.0.1:9092"
        val groupId = "consumer-kotlin-with-shutdown"
        val topic = "demo_kotlin"

        // create consumer configs
        val properties = Properties()
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer)
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        // options: none (if none previous offset are found then don't even start),
        //          earliest (read from the very beginning of the topic),
        //          latest (read only from the now of the topic)
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

        // create consumer
        val consumer = KafkaConsumer<String, String>(properties)

        // get a reference to the current thread
        val mainThread = Thread.currentThread()

        // Adding the shutdown hook
        Runtime.getRuntime().addShutdownHook(Thread {
            log.info("Detected a shutdown, let's exit by calling consumer.wakeup()...")
            consumer.wakeup()

            // join the main thread to allow the execution of the code in the main thread
            try {
                mainThread.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        })

        // subscribe consumer our topic(s)
        consumer.subscribe(listOf(topic))

        try {
            // poll for new data
            while (true) {
                //log.info("polling")
                val records = consumer.poll(Duration.ofMillis(1000))
                for (record in records) {
                    log.info("key ${record.key()}, value ${record.value()}")
                    log.info("partition ${record.partition()}, offset ${record.offset()}")
                }
            }
        } catch (e: WakeupException) {
            log.info("Wake up exception!")
        } catch (e: Exception) {
            log.error("Unexpected exception")
        } finally {
            consumer.close()
            log.info("The consumer is now gracefully closed")
        }

    }
}