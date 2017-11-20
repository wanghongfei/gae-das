package org.fh.gae.das.sender.kafka;

import org.fh.gae.das.sender.DasSender;
import org.fh.gae.das.template.DasSerializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "das.store.kafka", name = "enable", matchIfMissing = false, havingValue = "true")
public class KafkaSender implements DasSender {
    @Value("${das.store.kafka.topic}")
    private String topic;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Override
    public void send(DasSerializable data) {
        kafkaTemplate.send(topic, new String(data.serialize()));
    }
}
