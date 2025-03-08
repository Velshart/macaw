package me.mmtr.macaw;

import me.mmtr.macaw.data.ChatMessage;
import me.mmtr.macaw.enums.MessageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.lang.NonNull;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChatControllerWebSocketTests {

    @LocalServerPort
    private int serverPort;
    private String URL;

    private WebSocketStompClient stompClient;
    private BlockingQueue<ChatMessage> messageQueue;

    private final StompFrameHandler stompFrameHandler = new StompFrameHandler() {
        @NonNull
        @Override
        public Type getPayloadType(@NonNull StompHeaders headers) {
            return ChatMessage.class;
        }

        @Override
        public void handleFrame(@NonNull StompHeaders headers, Object payload) {
            messageQueue.offer((ChatMessage) payload);
        }
    };

    @BeforeEach
    public void setup() {
        URL = "ws://localhost:" + serverPort + "/ws";

        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        messageQueue = new LinkedBlockingQueue<>();
    }

    @Test
    public void shouldSuccessfullyReceiveMessageOnGivenTopic() throws Exception {
        StompSession session = stompClient.connectAsync(URL, new StompSessionHandlerAdapter() {
                })
                .get(3, TimeUnit.SECONDS);

        session.subscribe("/topic/unknown-dummy", stompFrameHandler);

        ChatMessage message = new ChatMessage("unknown", "Message Content", MessageType.CHAT);
        session.send("/app/chat/unknown-dummy", message);

        ChatMessage receivedMessage = messageQueue.poll(5, TimeUnit.SECONDS);

        Assertions.assertNotNull(receivedMessage);
        Assertions.assertEquals("Message Content", receivedMessage.getContent());
        Assertions.assertEquals("unknown", receivedMessage.getSender());
        Assertions.assertEquals(MessageType.CHAT, receivedMessage.getType());
    }
}
