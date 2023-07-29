package com.example.demos.kafka

import com.launchdarkly.eventsource.EventSource
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import java.net.URI
import java.util.Properties
import java.util.concurrent.TimeUnit

fun main() {
    WikimediaChangesProducer.main()
}

object WikimediaChangesProducer {

    fun main() {

        // create producer properties
        val properties = Properties()
        val bootstrapServers = "127.0.0.1:9092"
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)

        // having in account that in kafka 3.0 in +, the producer is idempotence by default
        // this is only for minor versions
        // set safe producer configs (kafka <= 2.8)
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true")
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all") // same as -1
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE.toString())

        // set high throughput producer configs
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20")
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, (32 * 1024).toString())
        properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy")


        // create the Producer
        val producer = KafkaProducer<String, String>(properties)

        val topic = "wikimedia.recentchange"
        val eventHandler = WikimediaChangeHandler(producer, topic)
        val url = "https://stream.wikimedia.org/v2/stream/recentchange"
        val eventSource = EventSource.Builder(eventHandler, URI.create(url)).build()

        // start the producer in other thread
        eventSource.start()

        // we produce for 10 minutes and block the program until then
        TimeUnit.MINUTES.sleep(10)

    }
}