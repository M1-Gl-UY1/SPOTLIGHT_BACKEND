package com.m1sigl.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "messages")
public class Message {
    @Id
    private String id;

    @Indexed
    private String conversationId;

    private String senderId;
    private String recipientId;
    private String content;

    private Date timestamp;

    private MessageStatus status;

    private String mediaUrl;
    private FileType type;
}
