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

    @Value("${rabbitmq.exchange.delete-notification.name}")
    private String exchangeDeleteName;

    //Sale Exchange
    @Value("${rabbitmq.exchange.sale.create-notification.name}")
    private String exchangeCreateSaleName;

    @Value("${rabbitmq.exchange.sale.update-notification.name}")
    private String exchangeUpdateSaleName;

    @Value("${rabbitmq.exchange.sale.delete-notification.name}")
    private String exchangeDeleteSaleName;


    @Value("${rabbitmq.queue.update-notification.name}")
    private String queueUpdateName;

    @Value("${rabbitmq.queue.delete-notification.name}")
    private String queueDeleteName;

    @Value("${rabbitmq.exchange.error.create.notification.name}")
    private String exchangeCreateErrorName;

    @Value("${rabbitmq.queue.error.create.notification.name}")
    private String queueCreateErrorName;

    @Value("${rabbitmq.exchange.update-error-notification.name}")
    private String exchangeUpdateErrorName;

    @Value("${rabbitmq.queue.update-error-notification.name}")
    private String queueUpdateErrorName;

    // Sale Queue
    @Value("${rabbitmq.queue.sale.create-notification.name}")
    private String queueCreateSaleName;

    @Value("${rabbitmq.queue.sale.update-notification.name}")
    private String queueUpdateSaleName;

    @Value("${rabbitmq.queue.sale.delete-notification.name}")
    private String queueDeleteSaleName;

    @Bean
    public FanoutExchange notificationCreateExchange(){
        return new FanoutExchange(exchangeCreateName);
    }

    @Bean
    public FanoutExchange notificationGetAllExchange(){
        return new FanoutExchange(exchangeGetAllName);
    }

    @Bean
    public FanoutExchange notificationUpdateExchange(){
        return new FanoutExchange(exchangeUpdateName);
    }

    @Bean
    public FanoutExchange notificationDeleteExchange(){
        return new FanoutExchange(exchangeDeleteName);
    }

    @Bean
    public FanoutExchange notificationUpdateErrorExchange(){
        return new FanoutExchange(exchangeUpdateErrorName);
    }

    @Bean
    public FanoutExchange notificationCreateErrorExchange(){
        return new FanoutExchange(exchangeCreateErrorName);
    }

    // Sale Exchange
    @Bean
    public FanoutExchange notificationCreateSaleExchange(){
        return new FanoutExchange(exchangeCreateSaleName);
    }

    @Bean
    public FanoutExchange notificationUpdateSaleExchange(){
        return new FanoutExchange(exchangeUpdateSaleName);
    }

    @Bean
    public FanoutExchange notificationDeleteSaleExchange(){
        return new FanoutExchange(exchangeDeleteSaleName);
    }

    @Bean
    public Queue notificationQueueCreateError() {
        return new Queue(queueCreateErrorName);
    }

    @Bean
    public Queue notificationQueueCreate() {
        return new Queue(queueCreateName);
    }

    @Bean
    public Queue notificationQueueGetAll() {
        return new Queue(queueGetAllName);
    }

    @Bean
    public Queue notificationQueueUpdate() {
        return new Queue(queueUpdateName);
    }

    @Bean
    public Queue notificationQueueDelete() {
        return new Queue(queueDeleteName);
    }

    @Bean
    public Queue notificationQueueUpdateError() {
        return new Queue(queueUpdateErrorName);
    }

    // Sale Queue
    @Bean
    public Queue notificationQueueCreateSale() {
        return new Queue(queueCreateSaleName);
    }

    @Bean
    public Queue notificationQueueUpdateSale() {
        return new Queue(queueUpdateSaleName);
    }

    @Bean
    public Queue notificationQueueDeleteSale() {
        return new Queue(queueDeleteSaleName);
    }

    @Bean
    public Binding bindingQueueCreate(){
        return BindingBuilder.bind(notificationQueueCreate()).to(notificationCreateExchange());
    }

    @Bean
    public Binding bindingQueueGetAll(){
        return BindingBuilder.bind(notificationQueueGetAll()).to(notificationGetAllExchange());
    }

    @Bean
    public Binding bindingQueueUpdate(){
        return BindingBuilder.bind(notificationQueueUpdate()).to(notificationUpdateExchange());
    }

    @Bean
    public Binding bindingQueueDelete(){
        return BindingBuilder.bind(notificationQueueDelete()).to(notificationDeleteExchange());
    }

    @Bean
    public Binding bindingQueueCreateError(){
        return BindingBuilder.bind(notificationQueueCreateError()).to(notificationCreateErrorExchange());
    }

    @Bean
    public Binding bindingQueueUpdateError(){
        return BindingBuilder.bind(notificationQueueUpdateError()).to(notificationUpdateErrorExchange());
    }

    //Sale Binding
    @Bean
    public Binding bindingQueueCreateSale(){
        return BindingBuilder.bind(notificationQueueCreateSale()).to(notificationCreateSaleExchange());
    }

    @Bean
    public Binding bindingQueueUpdateSale(){
        return BindingBuilder.bind(notificationQueueUpdateSale()).to(notificationUpdateSaleExchange());
    }

    @Bean
    public Binding bindingQueueDeleteSale(){
        return BindingBuilder.bind(notificationQueueDeleteSale()).to(notificationDeleteSaleExchange());
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
