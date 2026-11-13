package com.subacol.msagenda.config;

import com.subacol.msagenda.adapter.in.messaging.OrdenCerradaListener;
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
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue ordenCerradaQueue() {
        return QueueBuilder.durable(OrdenCerradaListener.COLA)
                .deadLetterExchange("")
                .deadLetterRoutingKey(OrdenCerradaListener.COLA + ".dlq")
                .build();
    }

    @Bean
    public Queue ordenCerradaDlq() {
        return QueueBuilder.durable(OrdenCerradaListener.COLA + ".dlq").build();
    }

    @Bean
    public Binding ordenCerradaBinding(Queue ordenCerradaQueue, TopicExchange exchange) {
        return BindingBuilder.bind(ordenCerradaQueue)
                .to(exchange)
                .with("orden.cerrada");
    }
}
