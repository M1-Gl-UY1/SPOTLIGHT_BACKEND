package com.m1sigl.chat.controller;

import com.m1sigl.chat.model.Conversation;
import com.m1sigl.chat.model.DeliveryStatus;
import com.m1sigl.chat.model.Message;
import com.m1sigl.chat.repository.ConversationRepository;
import com.m1sigl.chat.service.ChatMessageService;
import com.m1sigl.chat.service.FileStorageService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class ChatRestController {
    private final ChatMessageService chatMessageService;
    private final ConversationRepository conversationRepository;
    private final FileStorageService fileStorageService;

    @GetMapping("/messages/{senderId}/{recipientId}")
    public ResponseEntity<List<Message>> findChatMessages(@PathVariable String senderId, @PathVariable String recipientId) {
        return ResponseEntity.ok(chatMessageService.findChatMessages(senderId, recipientId));
    }

    @PostMapping("/Delivery/start/{conversationId}")
    public DeliveryStatus changeStatusStarted( @PathVariable String conversationId){
        Optional<Conversation> conversation = conversationRepository.findById(conversationId);
        conversation.get().setDeliveryStatus(DeliveryStatus.STARTED);
        return  conversation.get().getDeliveryStatus();
    }

    @PostMapping("/Delivery/finished/{conversationId}")
    public DeliveryStatus changeStatusDelivered( @PathVariable String conversationId){
        Optional<Conversation> conversation = conversationRepository.findById(conversationId);
        conversation.get().setDeliveryStatus(DeliveryStatus.DELIVERED);
        return  conversation.get().getDeliveryStatus();
    }

    @PostMapping("/Delivery/reset/{conversationId}")
    public DeliveryStatus changeStatusReset( @PathVariable String conversationId){
        Optional<Conversation> conversation = conversationRepository.findById(conversationId);
        conversation.get().setDeliveryStatus(DeliveryStatus.NONE);
        return  conversation.get().getDeliveryStatus();
    }


    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String filename = fileStorageService.saveFile(file);
            // On retourne l'URL complète accessible
            String fileUrl = "http://localhost:8080/files/" + filename;
            return ResponseEntity.ok(fileUrl);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Erreur upload: " + e.getMessage());
        }
    }
}
