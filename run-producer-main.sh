export NUM_PRODUCERS=50
export PRODUCER_PROPERTIES_FILE=local.properties

mvn compile exec:java -Dexec.mainClass="io.confluent.examples.manyclients.ProducerMain"
