package me.mmtr.macaw.controller;

import lombok.extern.slf4j.Slf4j;
import me.mmtr.macaw.data.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat/private")
    public void privateChat(ChatMessage message, Authentication authentication) {
        message.setSender(authentication.getName());

        messagingTemplate.convertAndSendToUser(
                message.getRecipient(),
                "/queue/private-" + message.getRecipient(),
                message
        );
    }
}
