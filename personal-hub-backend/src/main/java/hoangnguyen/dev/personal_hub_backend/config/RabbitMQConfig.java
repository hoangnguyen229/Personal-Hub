package hoangnguyen.dev.personal_hub_backend.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;

@Getter
@Configuration
public class RabbitMQConfig {
//    public static final String NOTIFICATION_QUEUE = "notification.queue";
//    public static final String NOTIFICATION_ROUTING_KEY = "notification.routing.key";
//    public static final String MESSAGE_QUEUE = "message.queue";
//    public static final String MESSAGE_ROUTING_KEY = "message.routing.key";
//    public static final String PERSONAL_HUB_EXCHANGE = "personal-hub.exchange";

    @Value("${rabbitmq.queue.notification.name}")
    private String notificationQueue;

    @Value("${rabbitmq.queue.message.name}")
    private String messageQueue;

    @Value("${rabbitmq.exchange.name}")
    private String personalHubExchange;

    @Value("${rabbitmq.routing.key.notification.name}")
    private String notificationRoutingKey;

    @Value("${rabbitmq.routing.key.message.name}")
    private String messageRoutingKey;

    @Bean
    public Queue notificationQueue() {
        return new Queue(notificationQueue, true);
    }

    @Bean
    public Queue messageQueue() {
        return new Queue(messageQueue, true);
    }

    @Bean
    public TopicExchange personalHubExchange() {
        return new TopicExchange(personalHubExchange);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange personalHubExchange) {
        return BindingBuilder
                .bind(notificationQueue)
                .to(personalHubExchange)
                .with(notificationRoutingKey);
    }

    @Bean
    public Binding messageBinding(Queue messageQueue, TopicExchange personalHubExchange) {
        return BindingBuilder
                .bind(messageQueue)
                .to(personalHubExchange)
                .with(messageRoutingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter jsonMessageConverter){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
