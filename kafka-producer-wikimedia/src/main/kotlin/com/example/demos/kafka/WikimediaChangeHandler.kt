package com.example.demos.kafka

import com.launchdarkly.eventsource.EventHandler
import com.launchdarkly.eventsource.MessageEvent
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory

class WikimediaChangeHandler(
    val kafkaProducer: KafkaProducer<String, String>,
    val topic: String
) : EventHandler{

    private val log = LoggerFactory.getLogger(WikimediaChangeHandler::class.java.simpleName)

    override fun onOpen() {
        // nothing here
    }

    override fun onClosed() {
        kafkaProducer.close()
    }

    override fun onMessage(event: String?, messageEvent: MessageEvent?) {
        log.info(messageEvent?.data)
        // asynchronous
        kafkaProducer.send(ProducerRecord(topic, messageEvent?.data))
    }

    override fun onComment(comment: String?) {
        // nothing here
    }

    override fun onError(t: Throwable?) {
        log.error("Error in stream reading $t")
    }
}