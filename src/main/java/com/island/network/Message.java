package com.island.network;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.island.model.Player;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {
    private static final AtomicLong messageIdGenerator = new AtomicLong(0);
    private static final ObjectMapper objectMapper;
    
    static {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(Player.class, new PlayerSerializer());
        module.addDeserializer(Player.class, new PlayerDeserializer());
        objectMapper.registerModule(module);
    }

    @JsonProperty("messageId")
    private final long messageId;
    
    @JsonProperty("type")
    private final MessageType type;
    
    @JsonProperty("roomId")
    private final String roomId;
    
    @JsonProperty("from")
    private final String from;
    
    @JsonProperty("to")
    private final String to;
    
    @JsonProperty("ack")
    private final boolean isAck;
    
    @JsonProperty("data")
    private final Map<String, Object> data;

    public static class MessageSerializationException extends RuntimeException {
        public MessageSerializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @JsonCreator
    public Message(
            @JsonProperty("messageId") long messageId,
            @JsonProperty("type") MessageType type,
            @JsonProperty("roomId") String roomId,
            @JsonProperty("from") String from,
            @JsonProperty("to") String to,
            @JsonProperty("isAck") boolean isAck,
            @JsonProperty("data") Map<String, Object> data) {
        this.messageId = messageId;
        this.type = type;
        this.roomId = roomId;
        this.from = from;
        this.to = to;
        this.isAck = isAck;
        this.data = data != null ? new HashMap<>(data) : new HashMap<>();
    }

    public Message(MessageType type, String roomId, String from) {
        this(messageIdGenerator.incrementAndGet(), type, roomId, from, null, false, null);
    }

    public Message(MessageType type, String roomId, String from, boolean isAck) {
        this(messageIdGenerator.incrementAndGet(), type, roomId, from, null, isAck, null);
    }

    public Message(MessageType type, String roomId, String from, String to) {
        this(messageIdGenerator.incrementAndGet(), type, roomId, from, to, false, null);
    }

    public Message addExtraData(String key, Object value) {
        if (key != null && value != null) {
            data.put(key, value);
        }
        return this;
    }

    public byte[] toBytes() {
        try {
            String jsonString = toString();
            System.out.println("Serializing message: " + jsonString);
            return jsonString.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Serialization error: " + e.getMessage());
            throw new MessageSerializationException("Failed to convert message to bytes", e);
        }
    }

    @Override
    public String toString() {
        try {
            String json = objectMapper.writeValueAsString(this);
            System.out.println("Message serialized to JSON: " + json);
            return json;
        } catch (Exception e) {
            System.err.println("JSON serialization error: " + e.getMessage());
            throw new MessageSerializationException("Failed to serialize message to JSON", e);
        }
    }

    public static Message fromString(String json) {
        try {
            System.out.println("Deserializing message from JSON: " + json);
            Message message = objectMapper.readValue(json, Message.class);
            if (message == null) {
                throw new MessageSerializationException("Deserialized message is null", null);
            }
            return message;
        } catch (Exception e) {
            System.err.println("JSON deserialization error: " + e.getMessage());
            throw new MessageSerializationException("Failed to deserialize message from JSON: " + e.getMessage(), e);
        }
    }

    // Getters
    public static long getMessageId() {
        return messageIdGenerator.get();
    }

    public MessageType getType() {
        return type;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public boolean isAck() {
        return isAck;
    }

    public Map<String, Object> getData() {
        return Collections.unmodifiableMap(data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return messageId == message.messageId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(messageId);
    }
}

