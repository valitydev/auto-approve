package dev.vality.auto.approve.deserializer;

import dev.vality.damsel.claim_management.Change;
import dev.vality.damsel.claim_management.ClaimUpdated;
import dev.vality.damsel.claim_management.Event;
import dev.vality.kafka.common.exception.KafkaSerializationException;
import dev.vality.kafka.common.serialization.AbstractThriftDeserializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventDeserializer extends AbstractThriftDeserializer<Event> {

    public Event deserialize(String s, byte[] bytes) {
        try {
            return super.deserialize(bytes, new Event());
        } catch (KafkaSerializationException e) {
            log.info("Ignore event with deserialization error");
            Event event = new Event();
            event.setChange(Change.updated(new ClaimUpdated()));
            return event;
        }
    }

}
