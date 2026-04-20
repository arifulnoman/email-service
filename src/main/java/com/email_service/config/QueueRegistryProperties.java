package com.email_service.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Binds the list of queue definitions from application.properties.
 * Each entry represents one producer application that publishes email
 * requests to this service. Adding a new producer requires only a new
 * indexed block in application.properties — no code changes needed.
 *
 * <pre>
 * rabbitmq.queues[0].name=email.hrms.queue
 * rabbitmq.queues[0].routing-key=email.hrms.routing.key
 * rabbitmq.queues[0].dead-letter-queue-name=email.hrms.dead-letter.queue
 * </pre>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "rabbitmq")
public class QueueRegistryProperties {

    private List<QueueDefinition> queues;

    @Getter
    @Setter
    public static class QueueDefinition {

        private String name;

        private String routingKey;

        private String deadLetterQueueName;
    }
}
