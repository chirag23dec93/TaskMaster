package com.airtribe.TaskMaster.config;

import com.airtribe.TaskMaster.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                logger.debug("Processing CONNECT command");
                String token = accessor.getFirstNativeHeader("Authorization");
                logger.debug("Authorization header: {}", token);
                
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);
                    
                    if (jwtUtil.validateToken(token)) {
                        String username = jwtUtil.getUsernameFromToken(token);
                        logger.debug("Valid token for user: {}", username);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        
                        UsernamePasswordAuthenticationToken auth = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        
                        accessor.setUser(auth);
                        accessor.setLeaveMutable(true);
                        logger.debug("Successfully authenticated user: {}", username);
                    } else {
                        logger.warn("Invalid token");
                    }
                } else {
                    logger.warn("No authorization token found");
                }
            } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                logger.debug("Processing SUBSCRIBE command for destination: {}", accessor.getDestination());
                if (accessor.getUser() != null) {
                    logger.debug("User {} subscribing to {}", accessor.getUser().getName(), accessor.getDestination());
                } else {
                    logger.warn("No user found in subscription request");
                }
            }
        }
        return message;
    }
}
