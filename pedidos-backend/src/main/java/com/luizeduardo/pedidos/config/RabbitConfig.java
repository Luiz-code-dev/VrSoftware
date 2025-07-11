package com.luizeduardo.pedidos.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String QUEUE_PEDIDOS = "pedidos.entrada.luiz-eduardo";
    public static final String QUEUE_DLQ = "pedidos.entrada.luiz-eduardo.dlq";
    public static final String QUEUE_STATUS_SUCESSO = "pedidos.status.sucesso.luiz-eduardo";
    public static final String QUEUE_STATUS_FALHA = "pedidos.status.falha.luiz-eduardo";
    
    @Bean
    public Queue pedidosQueue() {
        return QueueBuilder.durable(QUEUE_PEDIDOS)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", QUEUE_DLQ)
                .build();
    }

    @Bean
    public Queue pedidosDLQ() {
        return QueueBuilder.durable(QUEUE_DLQ).build();
    }

    @Bean
    public Queue statusSucessoQueue() {
        return QueueBuilder.durable(QUEUE_STATUS_SUCESSO).build();
    }

    @Bean
    public Queue statusFalhaQueue() {
        return QueueBuilder.durable(QUEUE_STATUS_FALHA).build();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
