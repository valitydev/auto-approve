package dev.vality.auto.approve.deserializer;

import dev.vality.damsel.claim_management.Change;
import dev.vality.damsel.claim_management.ClaimUpdated;
import dev.vality.damsel.claim_management.Event;
import dev.vality.kafka.common.exception.KafkaSerializationException;
import dev.vality.kafka.common.serialization.AbstractThriftDeserializer;

public class EventDeserializer extends AbstractThriftDeserializer<Event> {

    public Event deserialize(String s, byte[] bytes) {
        try {
            return super.deserialize(bytes, new Event());
        } catch (KafkaSerializationException e) {
            Event event = new Event();
            event.setChange(Change.updated(new ClaimUpdated()));
            return event;
        }
    }

}
