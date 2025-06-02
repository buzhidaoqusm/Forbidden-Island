package com.island.network;

import java.util.HashMap;
import java.util.Map;

/**
 * Message class represents a network message in the Forbidden Island game.
 * It handles message passing between players and game components.
 */
public class Message {
    /** Unique identifier for the message */
    private long messageId;
    
    /** Type of the message indicating its purpose */
    private MessageType type;
    
    /** Identifier of the game room this message belongs to */
    private int roomId;
    
    /** Username of the message sender */
    private String from;
    
    /** Username of the message recipient (null for broadcast messages) */
    private String to;
    
    /** Flag indicating whether this is an acknowledgment message */
    private boolean isAck;
    
    /** Additional data carried by the message as key-value pairs */
    private Map<String, Object> data = new HashMap<>();

    /**
     * Default constructor
     */
    public Message() {
    }

    /**
     * Copy constructor to create a deep copy of another message
     * @param message The message to copy from
     */
    public Message(Message message) {
        this.messageId = message.getMessageId();
        this.type = message.getType();
        this.roomId = message.getRoomId();
        this.from = message.getFrom();
        this.to = message.getTo();
        this.isAck = message.isAck();
        this.data = new HashMap<>(message.getData());
    }

    /**
     * Constructor for creating a basic message
     * @param messageType The type of the message
     * @param id Room identifier
     * @param username Sender's username
     */
    public Message(MessageType messageType, int id, String username) {
        messageId = System.nanoTime();
        this.type = messageType;
        this.roomId = id;
        this.from = username;
        this.isAck = false;
    }

    /**
     * Constructor for creating a message with acknowledgment flag
     * @param messageType The type of the message
     * @param id Room identifier
     * @param username Sender's username
     * @param isAck Whether this is an acknowledgment message
     */
    public Message(MessageType messageType, int id, String username, boolean isAck) {
        messageId = System.nanoTime();
        this.type = messageType;
        this.roomId = id;
        this.from = username;
        this.isAck = isAck;
    }

    /**
     * Constructor for creating a message with sender and recipient
     * @param messageType The type of the message
     * @param id Room identifier
     * @param from Sender's username
     * @param to Recipient's username
     */
    public Message(MessageType messageType, int id, String from, String to) {
        messageId = System.nanoTime();
        this.type = messageType;
        this.roomId = id;
        this.from = from;
        this.to = to;
        this.isAck = false;
    }

    /**
     * Constructor for creating a message with a specific message ID
     * @param messageId Unique message identifier
     * @param messageType The type of the message
     * @param id Room identifier
     * @param from Sender's username
     * @param to Recipient's username
     */
    public Message(long messageId, MessageType messageType, int id, String from, String to) {
        this.messageId = messageId;
        this.type = messageType;
        this.roomId = id;
        this.from = from;
        this.to = to;
    }

    /**
     * Constructor for creating a message with a specific message ID and acknowledgment flag
     * @param messageType The type of the message
     * @param id Room identifier
     * @param from Sender's username
     * @param to Recipient's username
     * @param isAck Whether this is an acknowledgment message
     */
    public Message(MessageType messageType, int id, String from, String to, boolean isAck) {
        this.messageId = System.nanoTime();
        this.type = messageType;
        this.roomId = id;
        this.from = from;
        this.to = to;
        this.isAck = isAck;
    }

    /**
     * Adds extra data to the message
     * @param key The key for the data
     * @param value The value to store
     */
    public void addExtraData(String key, Object value) {
        data.put(key, value);
    }

    /**
     * Converts the message to a string format for network transmission
     * Format: messageId|type|roomId|from|to|isAck|key1=value1|key2=value2|...
     * @return String representation of the message
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(messageId).append("|");
        sb.append(type.name()).append("|");
        sb.append(roomId).append("|");
        sb.append(from == null ? "" : from).append("|");
        sb.append(to == null ? "" : to).append("|");
        sb.append(isAck).append("|");

        // serialize data
        if (data != null && !data.isEmpty()) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                sb.append(entry.getKey()).append("=");
                sb.append(entry.getValue()).append("|");
            }
        }

        return sb.toString();
    }

    /**
     * Creates a Message object from its string representation
     * @param message String representation of the message
     * @return New Message object
     * @throws IllegalArgumentException if the message format is invalid
     */
    public static Message fromString(String message) {
        String[] parts = message.split("\\|", -1);  // use negative restriction to keep empty fields at the end
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

        // deserialize data
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

    /**
     * Sets the message type
     * @param type The message type to set
     */
    public void setType(MessageType type) {
        this.type = type;
    }

    /**
     * Sets the room ID
     * @param roomId The room ID to set
     */
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    /**
     * Sets the sender's username
     * @param from The sender's username
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Sets the recipient's username
     * @param to The recipient's username
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * Sets the extra data map
     * @param data The map containing extra data
     */
    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    /**
     * Gets the message type
     * @return The message type
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Gets the room ID
     * @return The room ID
     */
    public int getRoomId() {
        return roomId;
    }

    /**
     * Gets the sender's username
     * @return The sender's username
     */
    public String getFrom() {
        return from;
    }

    /**
     * Gets the recipient's username
     * @return The recipient's username
     */
    public String getTo() {
        return to;
    }

    /**
     * Gets the message ID
     * @return The message ID
     */
    public long getMessageId() {
        return messageId;
    }

    /**
     * Sets the message ID
     * @param messageId The message ID to set
     */
    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    /**
     * Checks if the message is an acknowledgment
     * @return true if the message is an acknowledgment, false otherwise
     */
    public boolean isAck() {
        return isAck;
    }

    /**
     * Sets the acknowledgment flag
     * @param isAck The acknowledgment flag to set
     */
    public void setIsAck(boolean isAck) {
        this.isAck = isAck;
    }

    /**
     * Gets the extra data map
     * @return Map containing extra data
     */
    public Map<String, Object> getData() {
        return data;
    }
}
