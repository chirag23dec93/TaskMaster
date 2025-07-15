package com.airtribe.TaskMaster.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import com.airtribe.TaskMaster.entity.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestNotificationController {

    private static final Logger logger = LoggerFactory.getLogger(TestNotificationController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/send-notification")
    public ResponseEntity<String> sendTestNotification(
            Authentication authentication,
            @RequestParam String message) {
        
        if (authentication == null) {
            return ResponseEntity.badRequest().body("Authentication required");
        }

        User user = (User) authentication.getPrincipal();
        String userId = user.getId();  // Get UUID instead of username
        
        try {
            // Create a message object
            Map<String, Object> messageObj = new HashMap<>();
            messageObj.put("type", "TEST");
            messageObj.put("message", message);
            messageObj.put("timestamp", new Date().getTime());
            messageObj.put("userId", userId);

            logger.debug("Creating notification with userId: {}", userId);

            // Send to general notifications topic
            messagingTemplate.convertAndSend("/topic/notifications", messageObj);
            logger.debug("Sent notification to /topic/notifications: {}", messageObj);

            return ResponseEntity.ok("Notifications sent successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
