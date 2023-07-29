package com.example.demos.kafka

import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import java.util.Properties


fun main() {
    ProducerDemo.main()
}

object ProducerDemo {
    private val log = LoggerFactory.getLogger(ProducerDemo::class.java.simpleName)

    fun main() {
        log.info("I am a Kafka producer")

        // create producer properties
        val properties = Properties()
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092")
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)

        // create the Producer
        val producer = KafkaProducer<String, String>(properties)

        // create a producer record
        val producerRecord = ProducerRecord<String, String>("demo_kotlin", "hello world")

        // send the data - asynchronous
        producer.send(producerRecord) // prepara para enviar


        // flush data - synchronous
        //producer.flush() // envían sincronicamente antes q el metodo termine

        // flush and close the Producer
        producer.close() // envía y cierra, no sería necesario el flush anterior
    }
}