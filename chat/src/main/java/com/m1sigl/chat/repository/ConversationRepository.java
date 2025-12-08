package com.m1sigl.chat.repository;

import com.m1sigl.chat.model.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends MongoRepository<Conversation,String> {
    List<Conversation> findByParticipantIds(String userId);

    // On utilise @Query pour faire une requête JSON MongoDB native
    // { 'participantIds': { $all: [id1, id2] } } signifie :
    // "Trouve un doc où le tableau participantIds contient TOUS ces éléments"
    @Query("{ 'participantIds': { $all: [?0, ?1] } }")
    Optional<Conversation> findConversationByParticipants(String senderId, String recipientId);

}
