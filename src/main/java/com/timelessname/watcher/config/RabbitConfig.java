package com.timelessname.watcher.config;

import java.util.UUID;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.BindingBuilder.TopicExchangeRoutingKeyConfigurer;
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

  @Value("${twitch.chat.exchangeName}")
  String exchangeName;

  String queueName = UUID.randomUUID().toString();

  @Bean
  Queue queue() {
    return new Queue(queueName,false,false,true);
  }

  @Bean
  TopicExchange exchange() {
    return new TopicExchange(exchangeName,false,false);
  }

  @Bean
  Binding binding(Queue queue, TopicExchange exchange) {
    return BindingBuilder.bind(queue).to(exchange).with("*.message");
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
