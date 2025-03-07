package me.mmtr.macaw;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.simp.annotation.support.SimpAnnotationMethodMessageHandler;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ContextChatControllerTests.TestConfiguration.class,
        ContextChatControllerTests.TestWebSocketConfiguration.class})
public class ContextChatControllerTests {

    @Configuration
    @EnableWebSocketMessageBroker
    static class TestWebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

        @Override
        public void configureMessageBroker(MessageBrokerRegistry registry) {
            registry.enableSimpleBroker("/topic");
            registry.setApplicationDestinationPrefixes("/app");
        }

        @Override
        public void registerStompEndpoints(StompEndpointRegistry registry) {
            registry.addEndpoint("/ws");
        }
    }

    @Configuration
    static class TestConfiguration implements ApplicationListener<ContextRefreshedEvent> {


        @Autowired
        private List<SubscribableChannel> channels;

        @Autowired
        private List<MessageHandler> handlers;

        @Override
        public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
            for (MessageHandler handler : handlers) {
                if (handler instanceof SimpAnnotationMethodMessageHandler) {
                    continue;
                }

                for (SubscribableChannel channel : channels) {
                    channel.unsubscribe(handler);
                }
            }
        }
    }
}
