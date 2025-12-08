package com.m1sigl.chat.service;

import com.m1sigl.chat.model.Conversation;
import com.m1sigl.chat.repository.ConversationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ChatRoomService {

    private final ConversationRepository conversationRepository;

    /**
     * @param createNewIfNotExists  Si true, on crée la conversation si elle manque.
     */
    public Optional<String> getChatRoomId(String senderId, String recipientId, boolean createNewIfNotExists) {

        // 1. On cherche la conversation
        return conversationRepository.findConversationByParticipants(senderId, recipientId)
                .map(Conversation::getId) // Si trouvée, on retourne l'ID
                .or(() -> {
                    // 2. Si PAS trouvée, on regarde si on doit la créer
                    if (createNewIfNotExists) {
                        String chatId = createChat(senderId, recipientId);
                        return Optional.of(chatId);
                    }
                    // Sinon, on renvoie vide
                    return Optional.empty();
                });
    }

    // Méthode privée pour gérer la création
    private String createChat(String senderId, String recipientId) {
        Conversation conversation = new Conversation();
        conversation.setParticipantIds(List.of(senderId, recipientId));

        Conversation savedConversation = conversationRepository.save(conversation);
        return savedConversation.getId();
    }
}