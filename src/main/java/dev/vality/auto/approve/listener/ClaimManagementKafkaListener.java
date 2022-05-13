package dev.vality.auto.approve.listener;

import dev.vality.auto.approve.handler.EventHandler;
import dev.vality.damsel.claim_management.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimManagementKafkaListener {

    private final EventHandler<Event> eventHandler;

    @KafkaListener(topics = "${kafka.topic}", containerFactory = "kafkaListenerContainerFactory")
    public void handle(Event event, Acknowledgment ack) {
        log.info("Got event {}", event);
        if (eventHandler.isAccept(event)) {
            eventHandler.handle(event);
        }
        ack.acknowledge();
        log.info("Event has been committed {}", event);
    }
}
