package com.m1sigl.chat.repository;

import com.m1sigl.chat.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message,String> {
    List<Message> findByConversationId(String conversationId);
}
