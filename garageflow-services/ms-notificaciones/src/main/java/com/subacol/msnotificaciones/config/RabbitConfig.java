package com.subacol.msnotificaciones.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "taller.eventos";

    private static final String COLA_AGENDADA = "notificaciones.cita-agendada";
    private static final String COLA_CANCELADA = "notificaciones.cita-cancelada";
    private static final String COLA_AGENDADA_DLQ = "notificaciones.cita-agendada.dlq";
    private static final String COLA_CANCELADA_DLQ = "notificaciones.cita-cancelada.dlq";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue colaAgendada() {
        return QueueBuilder.durable(COLA_AGENDADA)
                .deadLetterExchange("")
                .deadLetterRoutingKey(COLA_AGENDADA_DLQ)
                .build();
    }

    @Bean
    public Queue colaCancelada() {
        return QueueBuilder.durable(COLA_CANCELADA)
                .deadLetterExchange("")
                .deadLetterRoutingKey(COLA_CANCELADA_DLQ)
                .build();
    }

    @Bean
    public Queue colaAgendadaDlq() {
        return QueueBuilder.durable(COLA_AGENDADA_DLQ).build();
    }

    @Bean
    public Queue colaCanceladaDlq() {
        return QueueBuilder.durable(COLA_CANCELADA_DLQ).build();
    }

    @Bean
    public Binding bindingAgendada(Queue colaAgendada, TopicExchange exchange) {
        return BindingBuilder.bind(colaAgendada).to(exchange).with("cita.agendada");
    }

    @Bean
    public Binding bindingCancelada(Queue colaCancelada, TopicExchange exchange) {
        return BindingBuilder.bind(colaCancelada).to(exchange).with("cita.cancelada");
    }
}
