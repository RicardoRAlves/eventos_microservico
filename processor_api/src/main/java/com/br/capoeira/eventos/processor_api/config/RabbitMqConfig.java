package com.br.capoeira.eventos.processor_api.config;

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

    @Value("${rabbitmq.exchange.create.name}")
    private String exchangeCreateName;

    @Value("${rabbitmq.exchange.error.create.name}")
    private String exchangeCreateErrorName;

    @Value("${rabbitmq.exchange.get-all.name}")
    private String exchangeGetAllName;

    @Value("${rabbitmq.create.queue.name}")
    private String queueCreateName;

    @Value("${rabbitmq.get-all.queue.name}")
    private String queueGetAllName;

    @Value("${rabbitmq.exchange.update.name}")
    private String exchangeUpdate;

    @Value("${rabbitmq.exchange.delete.name}")
    private String exchangeDelete;

    @Value("${rabbitmq.update.queue.name}")
    private String queueUpdateName;

    @Value("${rabbitmq.delete.queue.name}")
    private String queueDeleteName;

    // Sale

    @Value("${rabbitmq.exchange.sale.create.name}")
    private String exchangeCreateSaleName;

    @Value("${rabbitmq.sale.create.queue.name}")
    private String queueCreateSaleName;

    @Value("${rabbitmq.exchange.sale.update.name}")
    private String exchangeUpdateSaleName;

    @Value("${rabbitmq.sale.update.queue.name}")
    private String queueUpdateSaleName;

    @Value("${rabbitmq.exchange.sale.delete.name}")
    private String exchangeDeleteSaleName;

    @Value("${rabbitmq.sale.delete.queue.name}")
    private String queueDeleteSaleName;

    //Notificarion Event
    @Value("${rabbitmq.exchange.create-notification.name}")
    private String createNotificationExchange;

    @Value("${rabbitmq.exchange.get-all-notification.name}")
    private String exchangeGetAllNotificationName;

    @Value("${rabbitmq.exchange.update-notification.name}")
    private String exchangeUpdateNotificationName;

    @Value("${rabbitmq.exchange.delete-notification.name}")
    private String exchangeDeleteNotificationName;

    @Value("${rabbitmq.exchange.update-error-notification.name}")
    private String exchangeUpdateErrorNotificationName;

    //Notification Sale
    @Value("${rabbitmq.exchange.sale.create-notification.name}")
    private String exchangeCreateSaleNotification;

    @Value("${rabbitmq.exchange.sale.update-notification.name}")
    private String exchangeUpdateSaleNotification;

    @Value("${rabbitmq.exchange.sale.delete-notification.name}")
    private String exchangeDeleteSaleNotification;

    @Bean
    public FanoutExchange eventCreateExchange(){
        return new FanoutExchange(exchangeCreateName);
    }

    @Bean
    public FanoutExchange eventCreateErrorExchange(){
        return new FanoutExchange(exchangeCreateErrorName);
    }

    @Bean
    public FanoutExchange eventGetAllExchange(){
        return new FanoutExchange(exchangeGetAllName);
    }

    @Bean
    public FanoutExchange updateExchange(){
        return new FanoutExchange(exchangeUpdate);
    }

    @Bean
    public FanoutExchange deleteExchange(){
        return new FanoutExchange(exchangeDelete);
    }

    //Sale Exchange

    @Bean
    public FanoutExchange createSaleExchange(){
        return new FanoutExchange(exchangeCreateSaleName);
    }

    @Bean
    public FanoutExchange updateSaleExchange(){
        return new FanoutExchange(exchangeUpdateSaleName);
    }

    @Bean
    public FanoutExchange deleteSaleExchange(){
        return new FanoutExchange(exchangeDeleteSaleName);
    }

    //Notification Exchange

    @Bean
    public FanoutExchange eventCreateNotificationExchange(){
        return new FanoutExchange(createNotificationExchange);
    }

    @Bean
    public FanoutExchange eventGetAllNotificationExchange(){
        return new FanoutExchange(exchangeGetAllNotificationName);
    }

    @Bean
    public FanoutExchange eventGerUpdateNotificationExchange(){
        return new FanoutExchange(exchangeUpdateNotificationName);
    }

    @Bean
    public FanoutExchange eventDeleteNotificationExchange(){
        return new FanoutExchange(exchangeDeleteNotificationName);
    }

    // Notification Sale Exchange
    @Bean
    public FanoutExchange eventSaleCreateNotificationExchange(){
        return new FanoutExchange(exchangeCreateSaleNotification);
    }

    @Bean
    public FanoutExchange eventSaleUpdateNotificationExchange(){
        return new FanoutExchange(exchangeUpdateSaleNotification);
    }

    @Bean
    public FanoutExchange eventSaleDeleteNotificationExchange(){
        return new FanoutExchange(exchangeDeleteSaleNotification);
    }

    // Queue

    @Bean
    public Queue processorQueueCreate() {
        return new Queue(queueCreateName);
    }

    @Bean
    public Queue processorQueueGetAll() {
        return new Queue(queueGetAllName);
    }

    @Bean
    public Queue processorQueueUpdate() {
        return new Queue(queueUpdateName);
    }

    @Bean
    public Queue processorQueueDelete() {
        return new Queue(queueDeleteName);
    }

    @Bean
    public Queue processorQueueCreateSale() {
        return new Queue(queueCreateSaleName);
    }

    @Bean
    public Queue processorQueueUpdateSale() {
        return new Queue(queueUpdateSaleName);
    }

    @Bean
    public Queue processorQueueDeleteSale() {
        return new Queue(queueDeleteSaleName);
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
    public Binding bindingQueueUpdate(){
        return BindingBuilder.bind(processorQueueUpdate()).to(updateExchange());
    }

    @Bean
    public Binding bindingQueueDelete(){
        return BindingBuilder.bind(processorQueueDelete()).to(deleteExchange());
    }

    @Bean
    public Binding bindingQueueCreateSale(){
        return BindingBuilder.bind(processorQueueCreateSale()).to(createSaleExchange());
    }

    @Bean
    public Binding bindingQueueUpdateSale(){
        return BindingBuilder.bind(processorQueueUpdateSale()).to(updateSaleExchange());
    }

    @Bean
    public Binding bindingQueueDeleteSale(){
        return BindingBuilder.bind(processorQueueDeleteSale()).to(deleteSaleExchange());
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
