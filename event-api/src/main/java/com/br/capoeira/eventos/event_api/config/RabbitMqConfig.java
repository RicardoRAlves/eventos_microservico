package com.br.capoeira.eventos.event_api.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${rabbitmq.exchange.create.name}")
    private String exchangeCreateName;

    @Value("${rabbitmq.exchange.get-all.name}")
    private String exchangeGetAll;

    @Value("${rabbitmq.exchange.error.create.name}")
    private String exchangeCreateError;

    @Value("${rabbitmq.exchange.error.create.notification.name}")
    private String exchangeCreateErrorNotification;

    @Value("${rabbitmq.exchange.update.name}")
    private String exchangeUpdate;

    @Value("${rabbitmq.create.error.queue.name}")
    private String queueErrorCreate;

    @Bean
    public Exchange createExchange(){
        return new FanoutExchange(exchangeCreateName);
    }

    @Bean
    public Exchange getAllExchange(){
        return new FanoutExchange(exchangeGetAll);
    }

    @Bean
    public FanoutExchange createErrorExchange(){
        return new FanoutExchange(exchangeCreateError);
    }

    @Bean
    public FanoutExchange createErrorNotificationExchange(){
        return new FanoutExchange(exchangeCreateErrorNotification);
    }

    @Bean
    public Exchange updateExchange(){
        return new FanoutExchange(exchangeUpdate);
    }

    @Bean
    public Queue queueError(){ return new Queue(queueErrorCreate);}

    @Bean
    public Binding binding() { return BindingBuilder.bind(queueError()).to(createErrorExchange());
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory){
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }


    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> applicationReadyEventApplicationListener(RabbitAdmin rabbitAdmin){
        return event -> rabbitAdmin.initialize();
    }
}
