package com.island.network;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper; // 使用 Jackson 的真实实现示例

public class Message {
    private static long messageId;
    private MessageType type;
    private String roomId;
    private String from;
    private String to;
    private boolean isAck;
    private Map<String, Object> data;

    public Message(MessageType type, String roomId, String from) {
        this.type = type;
        this.roomId = roomId;
        this.from = from;
        this.data = new HashMap<>();  // Initialization data
    }

    public Message(MessageType type, String roomId, String from, boolean isAck) {
        this(type, roomId, from);  // Reuse the previous constructor
        this.isAck = isAck;
    }

    public Message(MessageType type, String roomId, String from, String to) {
        this(type, roomId, from);  // Reuse basic constructor
        this.to = to;
    }

    public Message(long messageId, MessageType type, String roomId, String from, String to) {
        this(type, roomId, from, to);  // Reuse constructor with to
        this.messageId = messageId;
    }

    // Data operation methods
    public Message addExtraData(String key, Object value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(key, value);
        return null;
    }

    // Serialization / deserialization
    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception e) {
            return "{}";
        }
    }

    public static Message fromString(String json) {
        try {
            try {
                return new ObjectMapper().readValue(json, Message.class);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON");
        }
    }

    // Example methods in the Message class
    public byte[] toBytes() {
        return this.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static long getMessageId() { return messageId; }
    public MessageType getType() { return type; }
    public String getRoomId() { return roomId; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public boolean isAck() { return isAck; }
    public Map<String, Object> getData() { return data; }
}

