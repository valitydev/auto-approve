package dev.vality.auto.approve.configuration;

import dev.vality.auto.approve.configuration.properties.KafkaConsumerProperties;
import dev.vality.auto.approve.configuration.properties.KafkaSslProperties;
import dev.vality.auto.approve.deserializer.EventDeserializer;
import dev.vality.damsel.claim_management.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.*;
import org.springframework.util.backoff.FixedBackOff;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableConfigurationProperties({KafkaSslProperties.class, KafkaConsumerProperties.class})
@RequiredArgsConstructor
public class KafkaConfiguration {

    @Value("${kafka.bootstrap.servers}")
    private String bootstrapServers;

    @Value("${kafka.retry-policy.maxAttempts}")
    private int maxAttempts;

    @Bean
    public Map<String, Object> consumerConfigs(KafkaSslProperties kafkaSslProperties,
                                               KafkaConsumerProperties kafkaConsumerProperties) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerProperties.getGroupId());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaConsumerProperties.getClientId());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaConsumerProperties.isEnableAutoCommit());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaConsumerProperties.getAutoOffsetReset());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaConsumerProperties.getMaxPollRecords());
        props.put(ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, kafkaConsumerProperties.getConnectionsMaxIdleMs());
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaConsumerProperties.getSessionTimeoutMs());
        configureSsl(props, kafkaSslProperties);

        return props;
    }

    private void configureSsl(Map<String, Object> props, KafkaSslProperties kafkaSslProperties) {
        if (kafkaSslProperties.isEnabled()) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name());
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,
                    new File(kafkaSslProperties.getTrustStoreLocation()).getAbsolutePath());
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, kafkaSslProperties.getTrustStorePassword());
            props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, kafkaSslProperties.getKeyStoreType());
            props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, kafkaSslProperties.getTrustStoreType());
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,
                    new File(kafkaSslProperties.getKeyStoreLocation()).getAbsolutePath());
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, kafkaSslProperties.getKeyStorePassword());
            props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, kafkaSslProperties.getKeyPassword());
        }
    }

    @Bean
    public ConsumerFactory<String, Event> consumerFactory(KafkaSslProperties kafkaSslProperties,
                                                          KafkaConsumerProperties kafkaConsumerProperties) {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(kafkaSslProperties, kafkaConsumerProperties));
    }

    @Bean
    @SuppressWarnings("LineLength")
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Event>> kafkaListenerContainerFactory(
            ConsumerFactory<String, Event> consumerFactory,
            KafkaConsumerProperties kafkaConsumerProperties
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Event> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(kafkaErrorHandler());
        factory.setConcurrency(kafkaConsumerProperties.getConcurrency());
        return factory;
    }

    private CommonErrorHandler kafkaErrorHandler() {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(new FixedBackOff(0, maxAttempts));
        errorHandler.setAckAfterHandle(false);
        return errorHandler;
    }

}
