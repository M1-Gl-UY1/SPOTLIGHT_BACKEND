package com.m1sigl.chat.service;

import com.m1sigl.chat.model.Message;
import com.m1sigl.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final MessageRepository repository;
    private final ChatRoomService chatroomService;

    public Message save(Message message){
        return repository.save(message);
    }

    public List<Message> findChatMessages(String senderId, String recipientId){
        Optional<String> chatId = chatroomService.getChatRoomId(senderId, recipientId, false);
        return chatId.map(repository::findByConversationId).orElse(new ArrayList<>());
    }
}
