package dev.vality.auto.approve.deserializer;

import dev.vality.damsel.claim_management.Event;
import dev.vality.kafka.common.serialization.AbstractThriftDeserializer;

public class EventDeserializer extends AbstractThriftDeserializer<Event> {

    public Event deserialize(String s, byte[] bytes) {
        return super.deserialize(bytes, new Event());
    }

}
