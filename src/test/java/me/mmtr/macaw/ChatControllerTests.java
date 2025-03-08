package me.mmtr.macaw;

import me.mmtr.macaw.controller.ChatController;
import me.mmtr.macaw.data.ChatMessage;
import me.mmtr.macaw.enums.MessageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChatControllerTests {

    @Mock
    private Principal principal;

    @InjectMocks
    private ChatController chatController;

    @Test
    public void shouldThrowAnExceptionWhenRoomIdDoesNotContainUsersUsername() {
        when(principal.getName()).thenReturn("unpermitted");

        ChatMessage message = new ChatMessage("user1", "Message Content", MessageType.CHAT);


        IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () ->
                chatController.sendMessage(principal, "user1-user2", message));

        assertEquals("You are not permitted to enter this room.", thrownException.getMessage());
    }

}
