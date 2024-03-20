FROM openjdk:17
COPY many-producers-0.0.1-jar-with-dependencies.jar app.jar
ENV NUM_PRODUCERS 10
ENV PRODUCER_PROPERTIES_FILE local-producer.properties
ENTRYPOINT ["java","-cp","/app.jar", "io.confluent.examples.manyclients.MainProducer"]

