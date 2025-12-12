package com.m1sigl.chat.controller;

import com.m1sigl.chat.model.Conversation;
import com.m1sigl.chat.model.DeliveryStatus;
import com.m1sigl.chat.model.Message;
import com.m1sigl.chat.repository.ConversationRepository;
import com.m1sigl.chat.service.ChatMessageService;
import com.m1sigl.chat.service.ChatService; // Nécessaire pour le POST
import com.m1sigl.chat.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1") // Préfixe global standard
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatRestController {

    private final ChatMessageService chatMessageService;
    private final ConversationRepository conversationRepository;
    private final FileStorageService fileStorageService;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate; // Pour notifier le WS depuis le REST

    // 1. GET /messages (Historique)
    // Adaptation pour respecter le GET /messages du tableau tout en gardant tes params
    // URL: /api/v1/messages?senderId=A&recipientId=B
    @GetMapping("/messages")
    public ResponseEntity<List<Message>> findChatMessages(@RequestParam String senderId, 
                                                          @RequestParam String recipientId) {
        return ResponseEntity.ok(chatMessageService.findChatMessages(senderId, recipientId));
    }

    // 2. POST /messages (Envoi classique HTTP - Route demandée dans le tableau)
    @PostMapping("/messages")
    public ResponseEntity<Message> sendMessageRest(@RequestBody Message chatMessage) {
        // Logique identique au WebSocket (Sauvegarde + Création conversation)
        String conversationId = chatService.getOrCreateConversationId(
                chatMessage.getSenderId(),
                chatMessage.getRecipientId()
        );
        chatMessage.setConversationId(conversationId);
        Message savedMsg = chatMessageService.save(chatMessage);

        // On notifie quand même les clients connectés en WebSocket
        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipientId(),
                "/queue/messages",
                savedMsg // Ou un objet Notification
        );

        return ResponseEntity.ok(savedMsg);
    }

    // 3. START DELIVERY (Début cycle livraison)
    // URL: /api/v1/delivery/{convId}/start
    @PostMapping("/delivery/{conversationId}/start")
    public ResponseEntity<DeliveryStatus> changeStatusStarted(@PathVariable String conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation introuvable"));
        
        conversation.setDeliveryStatus(DeliveryStatus.STARTED);
        conversationRepository.save(conversation); // IMPORTANT : Sauvegarde en base !
        
        return ResponseEntity.ok(conversation.getDeliveryStatus());
    }

    // 4. FINISH DELIVERY (Fin cycle livraison)
    // URL: /api/v1/delivery/{convId}/finished
    @PostMapping("/delivery/{conversationId}/finished")
    public ResponseEntity<DeliveryStatus> changeStatusDelivered(@PathVariable String conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation introuvable"));
        
        conversation.setDeliveryStatus(DeliveryStatus.DELIVERED);
        conversationRepository.save(conversation); // IMPORTANT : Sauvegarde en base !
        
        return ResponseEntity.ok(conversation.getDeliveryStatus());
    }

    // Upload (Hors tableau mais nécessaire)
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String filename = fileStorageService.saveFile(file);
            // URL à adapter selon ton environnement (Gateway ou direct)
            String fileUrl = "http://localhost:8080/api/v1/files/" + filename;
            return ResponseEntity.ok(fileUrl);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Erreur upload: " + e.getMessage());
        }
    }
}