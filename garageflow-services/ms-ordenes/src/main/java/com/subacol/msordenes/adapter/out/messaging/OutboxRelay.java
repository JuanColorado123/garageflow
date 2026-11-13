package com.subacol.msordenes.adapter.out.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class OutboxRelay {
    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);
    public static final String EXCHANGE = "taller.eventos";

    private final OutboxJpaRepository jpa;
    private final RabbitTemplate rabbitTemplate;
    private final Clock clock;

    public OutboxRelay(OutboxJpaRepository jpa, RabbitTemplate rabbitTemplate, Clock clock) {
        this.jpa = jpa;
        this.rabbitTemplate = rabbitTemplate;
        this.clock = clock;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publicarPendientes() {
        var pendientes = jpa.findTop50ByPublicadoEnIsNullOrderByCreadoEnAsc();

        for (OutboxEntity fila : pendientes) {
            try {
                rabbitTemplate.convertAndSend(EXCHANGE, fila.getRoutingKey(), fila.getPayload());
                fila.marcarPublicado(LocalDateTime.now(clock));
                jpa.save(fila);
            } catch (AmqpException e) {
                log.warn("Error publicando evento {}: {}", fila.getId(), e.getMessage());
                break;
            }
        }
    }
}
