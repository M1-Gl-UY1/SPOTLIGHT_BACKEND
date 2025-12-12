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

    // Correspond à la spécification WS /chat (via /app/chat dans config STOMP)
    @MessageMapping("/chat")
    public void processMessage(@Payload Message chatMessage){
        
        // 1. Gestion Conversation ID
        String conversationId = chatService.getOrCreateConversationId(
                chatMessage.getSenderId(),
                chatMessage.getRecipientId()
        );

        chatMessage.setConversationId(conversationId);
        
        // 2. Sauvegarde MongoDB
        Message savedMsg = chatMessageService.save(chatMessage);

        // 3. Notification Temps Réel
        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipientId(),
                "/queue/messages",
                new ChatNotification(
                        savedMsg.getId(),
                        savedMsg.getSenderId(),
                        savedMsg.getSenderId() // Attention: vérifier si le 3ème paramètre est le nom ou l'ID
                )
        );
    }
}