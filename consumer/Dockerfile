FROM openjdk:17
COPY many-producers-0.0.1-jar-with-dependencies.jar app.jar
ENV REPLICATION_FACTOR 1
ENV PREFIX pref1
ENV NUM_CONSUMERS 10
ENV CONSUMER_PROPERTIES_FILE local-consumer.properties
ENV POLL_SLEEP_INTERVAL_MS 1000
ENTRYPOINT ["java","-cp","/app.jar", "io.confluent.examples.manyclients.MainConsumer"]

