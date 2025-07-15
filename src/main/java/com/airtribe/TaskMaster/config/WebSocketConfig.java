package com.airtribe.TaskMaster.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.airtribe.TaskMaster.security.JwtUtil;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    private final WebSocketAuthChannelInterceptor channelInterceptor;
    private final JwtUtil jwtUtil;
    private final ThreadPoolTaskScheduler taskScheduler;

    public WebSocketConfig(WebSocketAuthChannelInterceptor channelInterceptor, 
                          JwtUtil jwtUtil,
                          ThreadPoolTaskScheduler taskScheduler) {
        this.channelInterceptor = channelInterceptor;
        this.jwtUtil = jwtUtil;
        this.taskScheduler = taskScheduler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/user")
            .setHeartbeatValue(new long[]{10000, 10000})
            .setTaskScheduler(taskScheduler);
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
               .setAllowedOrigins("http://localhost:8080")
               .setHandshakeHandler(new DefaultHandshakeHandler())
               .addInterceptors(new HandshakeInterceptor() {
                   @Override
                   public boolean beforeHandshake(ServerHttpRequest request, 
                           org.springframework.http.server.ServerHttpResponse response, 
                           WebSocketHandler wsHandler, 
                           Map<String, Object> attributes) {
                       logger.debug("Handshake interceptor - Headers: " + request.getHeaders());
                       // Get JWT token from handshake request
                       String token = request.getHeaders().getFirst("Authorization");
                       if (token != null && token.startsWith("Bearer ")) {
                           token = token.substring(7);
                           if (jwtUtil.validateToken(token)) {
                               String username = jwtUtil.getUsernameFromToken(token);
                               attributes.put("username", username);
                               logger.debug("Added username to WebSocket session: " + username);
                               return true;
                           }
                       }
                       logger.debug("No valid JWT token found in handshake request");
                       return true; // Allow connection even without token, we'll handle auth at message level
                   }

                   @Override
                   public void afterHandshake(ServerHttpRequest request, 
                           org.springframework.http.server.ServerHttpResponse response, 
                           WebSocketHandler wsHandler, 
                           Exception exception) {
                   }
               })
               .setHandshakeHandler(new DefaultHandshakeHandler())
               .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(channelInterceptor);
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                logger.debug("Inbound message: {}", message);
                return message;
            }
        });
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                logger.debug("Outbound message: {}", message);
                return message;
            }
        });
    }
}
