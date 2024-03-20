package io.confluent.examples.manyclients;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Slf4j
public class MainProducer {

    private static final int NUM_PRODUCERS = Integer.parseInt(System.getenv("NUM_PRODUCERS"));
    private static final String TOPIC = "many-clients-topic";

    static byte[] value = new byte[Integer.parseInt(System.getenv("PAYLOAD_BYTES"))];

    private static final List<KafkaProducer<Integer, byte[]>> kafkaProducers = new ArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        log.info("Starting up");
        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    log.info("Shutting down producers {}", kafkaProducers.size());
                    kafkaProducers.forEach(
                            p -> {
                                log.info("Shutting down producer {}", p.toString());
                                p.close();
                            }
                    );
                })
        );
        initProducers();
        while (true) {
            sendData();
            Thread.sleep(1000);
        }
    }

    private static void sendData() {
        // TODO: if each producer sends data to only one partition (e.g. use the customer id as the key),
        // then we would have a fewer number of open TCP connections. (1 compared to 6 in a 6-broker cluster).
        // TODO: make the message size configurable, such that we can test what a larger payload would mean for broker load.
        log.info("Sending sample data of size {} bytes", System.getenv("PAYLOAD_BYTES"));
        for (int i = 0; i < kafkaProducers.size(); i++) {
            kafkaProducers.get(i).send(new ProducerRecord<>(TOPIC, i , value));
        }
    }

    private static void initProducers() throws IOException {
        Properties properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(System.getenv("PRODUCER_PROPERTIES_FILE"));
        properties.load(stream);
        for (int i = 0; i < NUM_PRODUCERS; i++) {
            properties.put("client.id", String.format("producer-%d", i));
            KafkaProducer<Integer, byte[]> producer = new KafkaProducer<>(properties);
            kafkaProducers.add(producer);
        }
    }
}

