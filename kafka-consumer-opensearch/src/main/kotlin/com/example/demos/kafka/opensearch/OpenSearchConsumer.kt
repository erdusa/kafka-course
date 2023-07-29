package com.example.demos.kafka.opensearch

import com.google.gson.JsonParser
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.opensearch.action.bulk.BulkRequest
import org.opensearch.action.index.IndexRequest
import org.opensearch.client.RequestOptions
import org.opensearch.client.RestClient
import org.opensearch.client.RestHighLevelClient
import org.opensearch.client.indices.CreateIndexRequest
import org.opensearch.client.indices.GetIndexRequest
import org.opensearch.common.xcontent.XContentType
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.net.URI
import java.time.Duration
import java.util.Collections
import java.util.Properties
import kotlin.concurrent.thread

fun main() {
    OpenSearchConsumer.main()
}

object OpenSearchConsumer {
    private val log = LoggerFactory.getLogger(OpenSearchConsumer::class.java.simpleName)

    fun main() {
        // first create an OpenSearch Client
        val openSearchClient = createOpenSearchClient()

        // create our kafka Client
        val consumer = createKafkaConsumer()

        // we need to create the index on OpenSearch if it doesn't exist already
        openSearchClient.use {
            consumer.use {
                val indexExist = openSearchClient.indices().exists(GetIndexRequest("wikimedia"), RequestOptions.DEFAULT)
                if (indexExist.not()) {
                    val createIndexRequest = CreateIndexRequest("wikimedia")
                    openSearchClient.indices().create(createIndexRequest, RequestOptions.DEFAULT)
                    log.info("The wikimedia index has been created")
                } else {
                    log.info("The wikimedia index already exists")
                }

                // we subscribe the consumer
                consumer.subscribe(Collections.singleton("wikimedia.recentchange"))

                while (true) {
                    val records = consumer.poll(Duration.ofMillis(3_000))
                    val recordCount = records.count()
                    log.info("received $recordCount record(s)")

                    // to send a bulk of data
                    val bulkRequest = BulkRequest()

                    for (record in records) {

                        // strategy 1 for idempotence
                        // create unique id
                        //val id = "${record.topic()}_${record.partition()}_${record.offset()}"

                        try {
                            // strategy 2 for idempotence
                            // extract the ID from the JSON value
                            val id = extractId(record.value())


                            // create index request to send openSearch
                            val indexRequest = IndexRequest("wikimedia")
                                .source(record.value(), XContentType.JSON)
                                .id(id) // using id makes messages processing idempotence

                            // we add request to send all together, instead of sending record by record
                            bulkRequest.add(indexRequest)

//                            // send the record into OpenSearch
//                            val response = openSearchClient.index(indexRequest, RequestOptions.DEFAULT)

//                            log.info(response.id)
                        } catch (_: Exception){}

                    }

                    if (bulkRequest.numberOfActions() > 0) {
                        val bulkResponse = openSearchClient.bulk(bulkRequest, RequestOptions.DEFAULT)
                        log.info("inserted ${bulkResponse.items.size} record(s)")
                        Thread.sleep(1_000)

                        // we need to commit manually because we disabled automatic commit in consumer properties
                        // see fun createKafkaConsumer() -> AUTO_OFFSET_RESET_CONFIG = false
                        // commits offset after the batch is consumed
                        consumer.commitSync()
                        log.info("Offsets have been committed")
                    }
                }
            }
        }

        // main code logic

        // close things
    }

    private fun createOpenSearchClient(): RestHighLevelClient {
        val connString = "http://localhost:9200"
        // we build a URI from the connection string
        val connUri = URI.create(connString)
        // extract login information if it exists
        val userInfo = connUri.userInfo

        return if (userInfo.isNullOrEmpty()) {
            // REST client without security
            RestHighLevelClient(RestClient.builder(HttpHost(connUri.host, connUri.port, connUri.scheme)))
        } else {
            // REST client with security
            val auth = userInfo.split(":")

            val cp = BasicCredentialsProvider()
            cp.setCredentials(AuthScope.ANY, UsernamePasswordCredentials(auth[0], auth[1]))
            RestHighLevelClient(
                RestClient.builder(HttpHost(connUri.host, connUri.port, connUri.scheme))
                    .setHttpClientConfigCallback { httpAsyncClientBuilder ->
                        httpAsyncClientBuilder.setDefaultCredentialsProvider(cp)
                            .setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy())
                    }
            )
        }

    }

    private fun createKafkaConsumer(): KafkaConsumer<String, String> {
        val bootstrapServer = "127.0.0.1:9092"
        val groupId = "consumer-opensearch-demo"

        // create consumer configs
        val properties = Properties()
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer)
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
        // consumer commits automatically every 5 seconds
        // we are going to disable, we must commit manually
        // otherwise if we restart the consumer, it's going to consume the same data again
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")

        // create consumer
        return KafkaConsumer<String, String>(properties)
    }

    private fun extractId(json: String?): String {
        // gson library
        return JsonParser.parseString(json)
            .asJsonObject["meta"]
            .asJsonObject["id"]
            .asString
    }
}