package com.email_service.listener;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.email_service.config.QueueRegistryProperties;
import com.email_service.config.QueueRegistryProperties.QueueDefinition;
import com.email_service.dto.EmailRequestDTO;
import com.email_service.service.EmailSenderService;
import tools.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Registers a RabbitMQ listener container for every queue entry declared in
 * {@code application.properties} at application startup.
 *
 * <p>
 * Jackson's {@link ObjectMapper} is used directly to deserialize the raw
 * JSON body into {@link EmailRequestDTO}, intentionally bypassing the AMQP
 * {@code __TypeId__} header. This makes the email service completely
 * decoupled from producer class names — producers may use any package
 * structure as long as the JSON payload matches the DTO contract.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicQueueListenerRegistrar implements ApplicationListener<ApplicationReadyEvent> {

    private final QueueRegistryProperties queueRegistry;

    private final SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory;

    private final ObjectMapper objectMapper;

    private final EmailSenderService emailSenderService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        List<QueueDefinition> queues = queueRegistry.getQueues();

        if (queues == null || queues.isEmpty()) {
            log.warn("No queues configured in rabbitmq.queues — email service will not listen to any queue.");
            return;
        }

        AtomicInteger count = new AtomicInteger(0);

        for (QueueDefinition definition : queues) {
            registerListenerForQueue(definition);
            count.incrementAndGet();
        }

        log.info("Dynamic queue listener registration complete | queues registered={}", count.get());
    }

    private void registerListenerForQueue(QueueDefinition definition) {
        SimpleMessageListenerContainer container = rabbitListenerContainerFactory
                .createListenerContainer();
        container.setQueueNames(definition.getName());
        container.setMessageListener(message -> {
            try {
                EmailRequestDTO request = objectMapper.readValue(message.getBody(), EmailRequestDTO.class);
                log.info("Email request received | queue={} | to={} | template={}",
                        definition.getName(), request.getTo(), request.getTemplateName());
                emailSenderService.send(request);
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize email request from queue: "
                        + definition.getName(), e);
            }
        });
        container.start();

        log.info("Listener registered | queue={}", definition.getName());
    }
}
