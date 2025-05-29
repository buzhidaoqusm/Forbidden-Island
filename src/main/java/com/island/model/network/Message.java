package com.forbiddenisland.models.network;

import java.util.HashMap;
import java.util.Map;

public class Message {
    private long messageId;
    private MessageType type;
    private int roomId;
    private String from;
    private String to;
    private boolean isAck;
    private Map<String, Object> data = new HashMap<>();

    public Message() {
    }

    public Message(Message message) {
        this.messageId = message.getMessageId();
        this.type = message.getType();
        this.roomId = message.getRoomId();
        this.from = message.getFrom();
        this.to = message.getTo();
        this.isAck = message.isAck();
        this.data = new HashMap<>(message.getData());
    }

    public Message(MessageType messageType, int id, String username) {
        messageId = System.nanoTime();
        this.type = messageType;
        this.roomId = id;
        this.from = username;
        this.isAck = false;
    }

    public Message(MessageType messageType, int id, String username, boolean isAck) {
        messageId = System.nanoTime();
        this.type = messageType;
        this.roomId = id;
        this.from = username;
        this.isAck = isAck;
    }

    public Message(MessageType messageType, int id, String from, String to) {
        messageId = System.nanoTime();
        this.type = messageType;
        this.roomId = id;
        this.from = from;
        this.to = to;
        this.isAck = false;
    }

    public Message(long messageId, MessageType messageType, int id, String from, String to) {
        this.messageId = messageId;
        this.type = messageType;
        this.roomId = id;
        this.from = from;
        this.to = to;
    }

    // 添加额外数据
    public void addExtraData(String key, Object value) {
        data.put(key, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(messageId).append("|");
        sb.append(type.name()).append("|");
        sb.append(roomId).append("|");
        sb.append(from == null ? "" : from).append("|");
        sb.append(to == null ? "" : to).append("|");
        sb.append(isAck).append("|");

        // 序列化 data
        if (data != null && !data.isEmpty()) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                sb.append(entry.getKey()).append("=");
                sb.append(entry.getValue()).append("|");
            }
        }

        return sb.toString();
    }

    public static Message fromString(String message) {
        String[] parts = message.split("\\|", -1);  // 使用负限制，保留尾部空字段
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid message format");
        }
        Message msg = new Message();
        msg.setMessageId(Long.parseLong(parts[0]));
        msg.setType(MessageType.valueOf(parts[1]));
        msg.setRoomId(Integer.parseInt(parts[2]));
        msg.setFrom(parts[3]);
        String toPlayer = parts[4].isEmpty() ? null : parts[4];
        msg.setTo(toPlayer);
        msg.setIsAck(Boolean.parseBoolean(parts[5]));

        // 反序列化 data
        for (int i = 6; i < parts.length; i++) {
            String part = parts[i];
            if (!part.isEmpty()) {
                String[] kv = parts[i].split("=", 2);
                if (kv.length != 2) {
                    throw new IllegalArgumentException("Invalid message format");
                }
                msg.addExtraData(kv[0], kv[1]);
            }
        }

        return msg;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public MessageType getType() {
        return type;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public boolean isAck() {
        return isAck;
    }

    public void setIsAck(boolean isAck) {
        this.isAck = isAck;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
