package com.subacol.msauditoria.config;

import com.subacol.msauditoria.adapter.in.messaging.EstadoOrdenCambiadoListener;
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

    @Bean
    public TopicExchange eventosExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue colaEstadoCambiado() {
        return QueueBuilder.durable(EstadoOrdenCambiadoListener.COLA)
                .deadLetterExchange("")
                .deadLetterRoutingKey(EstadoOrdenCambiadoListener.COLA + ".dlq")
                .build();
    }

    @Bean
    public Queue colaEstadoCambiadoDlq() {
        return QueueBuilder.durable(EstadoOrdenCambiadoListener.COLA + ".dlq")
                .build();
    }

    @Bean
    public Binding enlace() {
        return BindingBuilder.bind(colaEstadoCambiado())
                .to(eventosExchange())
                .with("orden.estado-cambiado");
    }
}
