package io.confluent.examples.manyclients;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;

// Risks:
// * larger payloads (1500 Bytes => 2000) 4500 was too much.
// * ACLs (Does adding 40000 ACLs have an impact on performance?)
// * Keeping Topic / User / ACL configuration in Sync
//  a good option would be an ACL Management Tool
// * TLS connections ?
// * Network connectivity (not our responsibility)
//   * enough throughput available?
//   * are Kafka connections allowed to the Data Center?

@Slf4j
public class MainConsumer {

    private static final int NUM_CONSUMERS = Integer.parseInt(System.getenv("NUM_CONSUMERS"));
    private static final String TOPIC_FORMAT = System.getenv("PREFIX") + "-%d";

    private static final List<KafkaConsumer<String, String>> kafkaConsumers = new ArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        log.info("Starting up");
        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    log.info("Shutting down producers {}", kafkaConsumers.size());
                    kafkaConsumers.forEach(
                            p -> {
                                log.info("Shutting down producer {}", p.toString());
                                p.close();
                            }
                    );
                })
        );
        deleteTopics();
        createTopics();
        initConsumers();
        while (true) {
            pollAll();
            Thread.sleep(Integer.parseInt(System.getenv("POLL_SLEEP_INTERVAL_MS")));
        }
    }

    private static void pollAll() {
        log.info("Polling all Consumers");
        kafkaConsumers.forEach(consumer -> {
                    var records = consumer.poll(Duration.ofMillis(0));
                    records.iterator().forEachRemaining( r -> log.info(r.value()));
                });
    }

    private static void deleteTopics() throws IOException {
        Properties properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(System.getenv("CONSUMER_PROPERTIES_FILE"));
        properties.load(stream);

        try(KafkaAdminClient adminClient = (KafkaAdminClient) AdminClient.create(properties)) {
            for (int i = 0; i < NUM_CONSUMERS; i++) {
                try {
                    var deleteResult = adminClient.deleteTopics(
                            Collections.singleton(String.format(TOPIC_FORMAT, i))
                    ).all().get();
                    log.info("Delete topic result: {}", deleteResult.toString());
                } catch (Exception e) {
                    log.warn(e.toString());
                }
            }
        }

    }

    private static void createTopics() throws IOException {
        Properties properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(System.getenv("CONSUMER_PROPERTIES_FILE"));
        properties.load(stream);

        try(KafkaAdminClient adminClient = (KafkaAdminClient) AdminClient.create(properties)) {
            // TODO: create topics in batches
            for (int i = 0; i < NUM_CONSUMERS; i++) {
                try {
                    var result = adminClient.createTopics(
                            Collections.singleton(
                                    new NewTopic(
                                            String.format(TOPIC_FORMAT, i),
                                            1,
                                            Short.parseShort(System.getenv("REPLICATION_FACTOR"))
                                    )
                            )
                    ).all().get();
                    log.info("Create topic result: {}", result.toString());
                } catch (Exception e) {
                    log.warn(e.toString());
                }
            }
        }
    }

    private static void initConsumers() throws IOException {
        Properties properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(System.getenv("CONSUMER_PROPERTIES_FILE"));
        properties.load(stream);
        for (int i = 0; i < NUM_CONSUMERS; i++) {
            log.info("Creating consumer {}", i);
            properties.put("group.id", String.format(System.getenv("PREFIX") + "-consumer-%d", i));
            properties.put("client.id", String.format(System.getenv("PREFIX") + "consumer-%d", i));
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
            consumer.subscribe(Collections.singletonList(String.format(TOPIC_FORMAT, i)));
            kafkaConsumers.add(consumer);
        }
    }
}
