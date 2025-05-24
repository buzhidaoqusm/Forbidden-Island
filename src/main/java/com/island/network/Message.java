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

    //-------------------------
    // 构造函数（4 个重载版本）
    //-------------------------
    public Message(MessageType type, String roomId, String from) {
        this.type = type;
        this.roomId = roomId;
        this.from = from;
        this.data = new HashMap<>();  // 初始化 data
    }

    public Message(MessageType type, String roomId, String from, boolean isAck) {
        this(type, roomId, from);  // 复用上一个构造函数
        this.isAck = isAck;
    }

    public Message(MessageType type, String roomId, String from, String to) {
        this(type, roomId, from);  // 复用基础构造函数
        this.to = to;
    }

    public Message(long messageId, MessageType type, String roomId, String from, String to) {
        this(type, roomId, from, to);  // 复用带 to 的构造函数
        this.messageId = messageId;
    }

    //-------------------------
    // 数据操作方法
    //-------------------------
    public Message addExtraData(String key, Object value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(key, value);
        return null;
    }

    //-------------------------
    // 序列化/反序列化方法
    //-------------------------

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

    // Message 类中的示例方法
    public byte[] toBytes() {
        return this.toString().getBytes(StandardCharsets.UTF_8); // 简单实现
    }

    //-------------------------
    // Getter 方法（可选，根据需求添加）
    //-------------------------
    public static long getMessageId() { return messageId; }
    public MessageType getType() { return type; }
    public String getRoomId() { return roomId; }
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public boolean isAck() { return isAck; }
    public Map<String, Object> getData() { return data; }
}

