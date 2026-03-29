package com.br.capoeira.eventos.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
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
    @Value("${rabbitmq.exchange.create-notification.name}")
    private String exchangeCreateName;

    @Value("${rabbitmq.queue.create-notification.name}")
    private String queueCreateName;

    @Value("${rabbitmq.exchange.get-all-notification.name}")
    private String exchangeGetAllName;

    @Value("${rabbitmq.queue.get-all-notification.name}")
    private String queueGetAllName;

    @Value("${rabbitmq.exchange.update-notification.name}")
    private String exchangeUpdateName;

    @Value("${rabbitmq.queue.update-notification.name}")
    private String queueUpdateName;

    @Value("${rabbitmq.exchange.error.create.notification.name}")
    private String exchangeCreateErrorName;

    @Value("${rabbitmq.queue.error.create.notification.name}")
    private String queueCreateErrorName;

    @Value("${rabbitmq.exchange.update-error-notification.name}")
    private String exchangeUpdateErrorName;

    @Value("${rabbitmq.queue.update-error-notification.name}")
    private String queueUpdateErrorName;

    @Bean
    public FanoutExchange notificationCreateExchange(){
        return new FanoutExchange(exchangeCreateName);
    }

    @Bean
    public Queue notificationQueueCreate() {
        return new Queue(queueCreateName);
    }

    @Bean
    public Binding bindingQueueCreate(){
        return BindingBuilder.bind(notificationQueueCreate()).to(notificationCreateExchange());
    }

    @Bean
    public FanoutExchange notificationGetAllExchange(){
        return new FanoutExchange(exchangeGetAllName);
    }

    @Bean
    public Queue notificationQueueGetAll() {
        return new Queue(queueGetAllName);
    }

    @Bean
    public Binding bindingQueueGetAll(){
        return BindingBuilder.bind(notificationQueueGetAll()).to(notificationGetAllExchange());
    }

    @Bean
    public FanoutExchange notificationUpdateExchange(){
        return new FanoutExchange(exchangeUpdateName);
    }

    @Bean
    public Queue notificationQueueUpdate() {
        return new Queue(queueUpdateName);
    }

    @Bean
    public Binding bindingQueueUpdate(){
        return BindingBuilder.bind(notificationQueueUpdate()).to(notificationUpdateExchange());
    }

    @Bean
    public FanoutExchange notificationCreateErrorExchange(){
        return new FanoutExchange(exchangeCreateErrorName);
    }

    @Bean
    public Queue notificationQueueCreateError() {
        return new Queue(queueCreateErrorName);
    }

    @Bean
    public Binding bindingQueueCreateError(){
        return BindingBuilder.bind(notificationQueueCreateError()).to(notificationCreateErrorExchange());
    }

    @Bean
    public FanoutExchange notificationUpdateErrorExchange(){
        return new FanoutExchange(exchangeUpdateErrorName);
    }

    @Bean
    public Queue notificationQueueUpdateError() {
        return new Queue(queueUpdateErrorName);
    }

    @Bean
    public Binding bindingQueueUpdateError(){
        return BindingBuilder.bind(notificationQueueUpdateError()).to(notificationUpdateErrorExchange());
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
