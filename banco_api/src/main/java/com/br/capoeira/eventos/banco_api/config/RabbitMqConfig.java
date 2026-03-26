package com.br.capoeira.eventos.banco_api.config;

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

    @Value("${rabbitmq.exchange.error.create.name}")
    private String exchangeCreateErrorName;

    @Value("${rabbitmq.exchange.create-notification.name}")
    private String createNotificationExchange;

    @Value("${rabbitmq.exchange.get-all.name}")
    private String exchangeGetAllName;

    @Value("${rabbitmq.create.queue.name}")
    private String queueCreateName;

    @Value("${rabbitmq.get-all.queue.name}")
    private String queueGetAllName;

    @Bean
    public FanoutExchange eventCreateExchange(){
        return new FanoutExchange(exchangeCreateName);
    }

    @Bean
    public FanoutExchange eventCreateErrorExchange(){
        return new FanoutExchange(exchangeCreateErrorName);
    }

    @Bean
    public FanoutExchange eventCreateNotificationoExchange(){
        return new FanoutExchange(createNotificationExchange);
    }

    @Bean
    public FanoutExchange eventGetAllExchange(){
        return new FanoutExchange(exchangeGetAllName);
    }

    @Bean
    public Queue processorQueueCreate() {
        return new Queue(queueCreateName);
    }

    @Bean
    public Queue processorQueueGetAll() {
        return new Queue(queueGetAllName);
    }

    @Bean
    public Binding bindingQueueCreate(){
        return BindingBuilder.bind(processorQueueCreate()).to(eventCreateExchange());
    }

    @Bean
    public Binding bindingQueueGetAll(){
        return BindingBuilder.bind(processorQueueGetAll()).to(eventGetAllExchange());
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
