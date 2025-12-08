package com.m1sigl.chat.controller;

import com.m1sigl.chat.model.ChatNotification;
import com.m1sigl.chat.model.Message;
import com.m1sigl.chat.service.ChatMessageService;
import com.m1sigl.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final ChatService chatService;

    @MessageMapping("/chat")
    public void processMessage(@Payload Message chatMessage){
        // 1. Sauvegarder dans MongoDB

        String conversationId = chatService.getOrCreateConversationId(
                chatMessage.getSenderId(),
                chatMessage.getRecipientId()
        );

        chatMessage.setConversationId(conversationId);
        Message savedMsg = chatMessageService.save(chatMessage);

        // 2. Notifier le destinataire via RabbitMQ -> WebSocket
        // Le message sera envoyé sur : /user/{recipientId}/queue/messages
        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipientId(),
                "/queue/messages",
                new ChatNotification(
                        savedMsg.getId(),
                        savedMsg.getSenderId(),
                        savedMsg.getSenderId()
                )
        );
    }
}
