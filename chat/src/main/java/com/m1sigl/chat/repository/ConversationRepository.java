package com.m1sigl.chat.repository;

import com.m1sigl.chat.model.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends MongoRepository<Conversation,String> {
    List<Conversation> findByParticipantIds(String userId);

    @Query("{ 'participantIds': { $all: [?0, ?1] } }")
    Optional<Conversation> findExistingConversation(String user1, String user2);
}
