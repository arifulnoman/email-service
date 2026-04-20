package com.email_service.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.email_service.config.QueueRegistryProperties.QueueDefinition;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.email}")
    private String emailExchange;

    /**
     * Shared direct exchange — all queues bind to this single exchange.
     * Producers only need to know the exchange name and their routing key.
     */
    @Bean
    DirectExchange emailExchange() {
        return new DirectExchange(emailExchange);
    }

    /**
     * Dynamically declares all queues, dead-letter queues, and their
     * bindings from the configured queue registry.
     */
    @Bean
    Declarables emailQueueDeclarables(
            QueueRegistryProperties queueRegistry,
            DirectExchange emailExchange) {

        List<Declarable> declarables = new ArrayList<>();

        for (QueueDefinition definition : queueRegistry.getQueues()) {
            Queue mainQueue = QueueBuilder.durable(definition.getName())
                    .withArgument("x-dead-letter-exchange", emailExchange.getName())
                    .withArgument("x-dead-letter-routing-key", definition.getDeadLetterQueueName())
                    .build();

            Queue deadLetterQueue = QueueBuilder.durable(definition.getDeadLetterQueueName()).build();

            Binding mainBinding = BindingBuilder
                    .bind(mainQueue)
                    .to(emailExchange)
                    .with(definition.getRoutingKey());

            Binding deadLetterBinding = BindingBuilder
                    .bind(deadLetterQueue)
                    .to(emailExchange)
                    .with(definition.getDeadLetterQueueName());

            declarables.add(mainQueue);
            declarables.add(deadLetterQueue);
            declarables.add(mainBinding);
            declarables.add(deadLetterBinding);
        }

        return new Declarables(declarables);
    }

    @Bean
    JacksonJsonMessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
