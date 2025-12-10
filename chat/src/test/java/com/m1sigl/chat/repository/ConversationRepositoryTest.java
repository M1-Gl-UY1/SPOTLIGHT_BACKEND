package com.m1sigl.chat.repository;


import com.m1sigl.chat.model.Conversation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ExtendWith(SpringExtension.class)
public class ConversationRepositoryTest {
    @Autowired
    private ConversationRepository conversationRepository;

    @Test
    void shouldSaveAndFindConversation(){
        // 1. (ARRANGE)
        String userA = "user_1";
        String userB = "user_2";

        Conversation conversation = new Conversation();
        conversation.setParticipantIds(List.of(userA, userB));

        // 2. (ACT)
        Conversation savedConv = conversationRepository.save(conversation);

        // 3. (ASSERT)
        assertThat(savedConv.getId()).isNotNull(); // MongoDB a généré un ID

        // On teste notre méthode custom findByParticipantIds
        List<Conversation> foundConvs = conversationRepository.findByParticipantIds(userA);
        assertThat(foundConvs).hasSize(1);
        assertThat(foundConvs.get(0).getParticipantIds()).contains(userB);
    }
}
