package com.example.demos.kafka

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import java.util.Properties


fun main() {
    ProducerDemoKeys.main()
}

object ProducerDemoKeys {
    private val log = LoggerFactory.getLogger(ProducerDemoKeys::class.java.simpleName)

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

            val topic = "demo_kotlin"
            val value = "hello world $i"
            val key = "id_$i"

            // create a producer record
            val producerRecord = ProducerRecord(topic, key, value)

            // send the data - asynchronous
            producer.send(producerRecord) { metadata, exception ->
                // Execute every time a record is successfully sent or an exception is thrown
                if (exception == null) {
                    // the record was successfully sent
                    log.info(
                        """Received new metadata
                    topic: ${metadata.topic()}
                    key: ${producerRecord.key()}
                    partition: ${metadata.partition()}
                    offset: ${metadata.offset()}
                    timestamp: ${metadata.timestamp()}
                    """.trimIndent()
                    )
                } else {
                    log.error("Error while producing", exception)
                }
            }
        }

        // flush data - synchronous
        producer.flush() // envían sincronicamente antes q el metodo termine

        // flush and close the Producer
        producer.close() // envía y cierra, no sería necesario el flush anterior
    }
}