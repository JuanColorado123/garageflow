package com.subacol.msagenda.adapter.out.messaging;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxRelay {
    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);
    public static final String EXCHANGE = "taller.eventos";
    private final OutboxJpaRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final Clock clock;

    public OutboxRelay(
            OutboxJpaRepository repository, RabbitTemplate rabbitTemplate, Clock clock) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
        this.clock = clock;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void relay() {
        List<OutboxEntity> pendientes = repository.findTop50ByPublicadoEnIsNullOrderByCreadoEnAsc();
        for (OutboxEntity row : pendientes) {
            try {
                rabbitTemplate.convertAndSend(EXCHANGE, row.getRoutingKey(), row.getPayload());
                row.setPublicadoEn(LocalDateTime.now(clock).truncatedTo(ChronoUnit.MILLIS));
                repository.save(row);
            } catch (AmqpException e) {
                log.warn("Error publicando evento {}: {}", row.getId(), e.getMessage());
                break;
            }
        }
    }
}
