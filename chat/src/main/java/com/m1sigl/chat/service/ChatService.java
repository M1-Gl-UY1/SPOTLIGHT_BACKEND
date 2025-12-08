package com.m1sigl.chat.service;

import com.m1sigl.chat.model.Conversation;
import com.m1sigl.chat.model.Message;
import com.m1sigl.chat.model.MessageStatus;
import com.m1sigl.chat.repository.ConversationRepository;
import com.m1sigl.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    //Récupère l'ID d'une conversation existante ou en crée une nouvelle
    public String getOrCreateConversationId(String senderId, String recipientId){
        return conversationRepository.findConversationByParticipants(senderId,recipientId)
                .map(Conversation::getId)
                .orElseGet(()->{
                   Conversation newConv = new Conversation();
                   newConv.setParticipantIds(List.of(senderId, recipientId));

                   return conversationRepository.save(newConv).getId();
                });
    }

    //Sauvegarde le message en base
    public Message saveMessage(Message message){
        message.setStatus(MessageStatus.RECEIVED);
        message.setTimestamp(new Date());

        return  messageRepository.save(message);
    }
}
