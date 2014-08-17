package com.timelessname.watcher.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.timelessname.watcher.service.PricingService;


@Configuration
public class RabbitConfig {


  @Value("${rabbit.watcher.queueName}")
  String queueName;

  @Value("${rabbit.watcher.exchangeName}")
  String exchangeName;


  @Bean
  Queue queue() {
    return new Queue(queueName,false,false,true);
  }

  @Bean
  FanoutExchange exchange() {
    return new FanoutExchange(exchangeName,false,false);
  }

  @Bean
  Binding binding(Queue queue, FanoutExchange exchange) {
    return BindingBuilder.bind(queue).to(exchange);
  }

  @Bean
  SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.setQueueNames(queueName);
    container.setMessageListener(listenerAdapter);
    container.setAcknowledgeMode(AcknowledgeMode.NONE);
    return container;
  }



  @Bean
  MessageListenerAdapter listenerAdapter(PricingService pricingService) {
    return new MessageListenerAdapter(pricingService, "receiveMessage");
  }

  
}
