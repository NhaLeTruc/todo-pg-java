package com.todoapp.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String EMAIL_QUEUE = "email.queue";
  public static final String EMAIL_EXCHANGE = "email.exchange";
  public static final String EMAIL_ROUTING_KEY = "email.routing.key";

  public static final String VIRUS_SCAN_QUEUE = "virus-scan.queue";
  public static final String VIRUS_SCAN_EXCHANGE = "virus-scan.exchange";
  public static final String VIRUS_SCAN_ROUTING_KEY = "virus-scan.routing.key";

  public static final String RECURRENCE_QUEUE = "recurrence.queue";
  public static final String RECURRENCE_EXCHANGE = "recurrence.exchange";
  public static final String RECURRENCE_ROUTING_KEY = "recurrence.routing.key";

  public static final String NOTIFICATION_QUEUE = "notification.queue";
  public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
  public static final String NOTIFICATION_ROUTING_KEY = "notification.routing.key";

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(jsonMessageConverter());
    return rabbitTemplate;
  }

  @Bean
  public Queue emailQueue() {
    return QueueBuilder.durable(EMAIL_QUEUE)
        .withArgument("x-dead-letter-exchange", EMAIL_EXCHANGE + ".dlx")
        .withArgument("x-message-ttl", 3600000)
        .build();
  }

  @Bean
  public DirectExchange emailExchange() {
    return new DirectExchange(EMAIL_EXCHANGE);
  }

  @Bean
  public Binding emailBinding(Queue emailQueue, DirectExchange emailExchange) {
    return BindingBuilder.bind(emailQueue).to(emailExchange).with(EMAIL_ROUTING_KEY);
  }

  @Bean
  public Queue virusScanQueue() {
    return QueueBuilder.durable(VIRUS_SCAN_QUEUE)
        .withArgument("x-dead-letter-exchange", VIRUS_SCAN_EXCHANGE + ".dlx")
        .withArgument("x-message-ttl", 3600000)
        .build();
  }

  @Bean
  public DirectExchange virusScanExchange() {
    return new DirectExchange(VIRUS_SCAN_EXCHANGE);
  }

  @Bean
  public Binding virusScanBinding(Queue virusScanQueue, DirectExchange virusScanExchange) {
    return BindingBuilder.bind(virusScanQueue).to(virusScanExchange).with(VIRUS_SCAN_ROUTING_KEY);
  }

  @Bean
  public Queue recurrenceQueue() {
    return QueueBuilder.durable(RECURRENCE_QUEUE)
        .withArgument("x-dead-letter-exchange", RECURRENCE_EXCHANGE + ".dlx")
        .build();
  }

  @Bean
  public DirectExchange recurrenceExchange() {
    return new DirectExchange(RECURRENCE_EXCHANGE);
  }

  @Bean
  public Binding recurrenceBinding(Queue recurrenceQueue, DirectExchange recurrenceExchange) {
    return BindingBuilder.bind(recurrenceQueue)
        .to(recurrenceExchange)
        .with(RECURRENCE_ROUTING_KEY);
  }

  @Bean
  public Queue notificationQueue() {
    return QueueBuilder.durable(NOTIFICATION_QUEUE)
        .withArgument("x-dead-letter-exchange", NOTIFICATION_EXCHANGE + ".dlx")
        .withArgument("x-message-ttl", 3600000)
        .build();
  }

  @Bean
  public DirectExchange notificationExchange() {
    return new DirectExchange(NOTIFICATION_EXCHANGE);
  }

  @Bean
  public Binding notificationBinding(
      Queue notificationQueue, DirectExchange notificationExchange) {
    return BindingBuilder.bind(notificationQueue)
        .to(notificationExchange)
        .with(NOTIFICATION_ROUTING_KEY);
  }
}
