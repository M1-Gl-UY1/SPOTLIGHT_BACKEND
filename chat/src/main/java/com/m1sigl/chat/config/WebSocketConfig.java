package com.m1sigl.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // a Restreindre en Prod !
                .withSockJS(); // Active le mode fallback si le navigateur ne supporte pas WS natif
    }

    @Override
    public  void configureMessageBroker(MessageBrokerRegistry registry){
        // Préfixe pour les routes destinées au serveur (@MessageMapping)
        registry.setApplicationDestinationPrefixes("/app");

        // Configuration du BROKER (Relais vers RabbitMQ)
        // Les clients s'abonneront à des topics commençant par /user ou /topic
        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost("localhost")
                .setRelayPort(61613) // Port STOMP du RabbitMQ Docker
                .setClientLogin("guest")
                .setClientPasscode("guest");

        // Préfixe spécifique pour les messages privés (1-to-1)
        registry.setUserDestinationPrefix("/user");
    }
}
