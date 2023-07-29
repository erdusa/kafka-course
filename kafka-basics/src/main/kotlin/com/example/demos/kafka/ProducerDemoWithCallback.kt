package com.example.demos.kafka

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import java.util.Properties


fun main() {
    ProducerDemoWithCallback.main()
}

object ProducerDemoWithCallback {
    private val log = LoggerFactory.getLogger(ProducerDemoWithCallback::class.java.simpleName)

    fun main() {
        log.info("I am a Kafka producer")

        // create producer properties
        val properties = Properties()
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092")
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)

        // create the Producer
        val producer = KafkaProducer<String, String>(properties)

        for (i in 1..10) {
            // create a producer record
            val producerRecord = ProducerRecord<String, String>("demo_kotlin", "hello world $i")

            // send the data - asynchronous
            producer.send(producerRecord) { metadata, exception ->
                // Execute every time a record is successfully sent or an exception is thrown
                if (exception == null) {
                    // the record was successfully sent
                    log.info(
                        """Received new metadata
                    topic: ${metadata.topic()}
                    partition: ${metadata.partition()}
                    offset: ${metadata.offset()}
                    timestamp: ${metadata.timestamp()}
                    """.trimIndent()
                    )
                } else {
                    log.error("Error while producing", exception)
                }
            }

            // as all records are sent together (in batch),
            // then the producer uses stickyParticioner to send the data to the same partition to optimize the process.
            // for avoid this (to study purposes), let's put a sleep to avoid sending them in batch
            Thread.sleep(1000)
        }

        // flush data - synchronous
        producer.flush() // envían sincronicamente antes q el metodo termine

        // flush and close the Producer
        producer.close() // envía y cierra, no sería necesario el flush anterior
    }
}