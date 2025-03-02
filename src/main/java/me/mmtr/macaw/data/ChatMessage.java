package me.mmtr.macaw.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.mmtr.macaw.enums.MessageType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {

    private String sender;

    private String recipient;

    private String content;

    private MessageType type;
}
