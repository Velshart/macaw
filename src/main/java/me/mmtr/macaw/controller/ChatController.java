package me.mmtr.macaw.controller;

import me.mmtr.macaw.data.ChatMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatController {
    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage sendMessage(Principal user,
                                   @DestinationVariable String roomId,
                                   ChatMessage message) {
        if (!roomId.contains(user.getName())) {
            throw new IllegalArgumentException("You are not permitted to enter this room.");
        }
        return message;
    }
}
