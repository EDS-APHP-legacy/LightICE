package export.writers;

import common.DeviceIdentity;
import datatypes.Data;
import export.serializers.Serializer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Properties;

public class KafkaWriter extends Writer {
    private org.apache.kafka.clients.producer.Producer producer;
    private String topic;

    public KafkaWriter(Serializer serializer, String kafkaTopic, String kafkaJaasConfPath, Properties kafkaProps) {
        super(serializer);

        if (!Objects.equals(kafkaJaasConfPath, "") && kafkaJaasConfPath != null)
            System.setProperty("java.security.auth.login.config", kafkaJaasConfPath);

        if (serializer.outputType == null) {

        }
        else if (serializer.outputType == String.class) {
            kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            this.producer = new org.apache.kafka.clients.producer.KafkaProducer<String, String>(kafkaProps);
        }
        else if (serializer.outputType == ByteBuffer.class) {
            kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
            kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
            this.producer = new org.apache.kafka.clients.producer.KafkaProducer<String, ByteBuffer>(kafkaProps);
        }

        this.topic = kafkaTopic;
    }

    @Override
    public void write(DeviceIdentity deviceIdentity, Data data) {
        if (serializer.outputType == String.class) {
            for (String s : this.serializer.serializeToString(deviceIdentity, data))
                this.producer.send(new ProducerRecord<>(this.topic, s));
        }
        else if (serializer.outputType == ByteBuffer.class) {
            for (byte[] s : this.serializer.serializeToBytes(deviceIdentity, data))
                this.producer.send(new ProducerRecord<>(this.topic, s));
        }
    }
}
