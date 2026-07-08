
package com.mine.safety.config;

import com.mine.safety.service.impl.WebSocketServiceImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.UUID;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    private final WebSocketServiceImpl webSocketService;
    
    public WebSocketConfig(WebSocketServiceImpl webSocketService) {
        this.webSocketService = webSocketService;
    }
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new TextWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(org.springframework.web.socket.WebSocketSession session) {
                String sessionId = UUID.randomUUID().toString();
                webSocketService.registerSession(sessionId, session);
            }
            
            @Override
            public void afterConnectionClosed(org.springframework.web.socket.WebSocketSession session, 
                                              org.springframework.web.socket.CloseStatus status) {
                webSocketService.unregisterSession(session.getId());
            }
        }, "/ws/safety").setAllowedOrigins("*");
    }
}
