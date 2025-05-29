package com.island.network;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Message functionality
 */
class MessageTest {

    /**
     * Tests the basic constructor with minimal parameters
     */
    @Test
    void testBasicConstructor() {
        Message message = new Message(MessageType.PLAYER_JOIN, 1, "TestPlayer");
        
        assertEquals(MessageType.PLAYER_JOIN, message.getType());
        assertEquals(1, message.getRoomId());
        assertEquals("TestPlayer", message.getFrom());
        assertFalse(message.isAck());
        assertNotNull(message.getData());
        assertTrue(message.getData().isEmpty());
    }

    /**
     * Tests constructor with acknowledgment flag
     */
    @Test
    void testConstructorWithAck() {
        Message message = new Message(MessageType.PLAYER_JOIN, 1, "TestPlayer", true);
        
        assertTrue(message.isAck());
        assertEquals("TestPlayer", message.getFrom());
    }

    /**
     * Tests constructor with recipient field
     */
    @Test
    void testConstructorWithToField() {
        Message message = new Message(MessageType.PLAYER_JOIN, 1, "FromPlayer", "ToPlayer");
        
        assertEquals("FromPlayer", message.getFrom());
        assertEquals("ToPlayer", message.getTo());
    }

    /**
     * Tests the copy constructor for deep copying
     */
    @Test
    void testCopyConstructor() {
        Message original = new Message(MessageType.PLAYER_JOIN, 1, "TestPlayer", true);
        original.addExtraData("key1", "value1");
        
        Message copy = new Message(original);
        
        assertEquals(original.getMessageId(), copy.getMessageId());
        assertEquals(original.getType(), copy.getType());
        assertEquals(original.getRoomId(), copy.getRoomId());
        assertEquals(original.getFrom(), copy.getFrom());
        assertEquals(original.isAck(), copy.isAck());
        assertEquals(original.getData(), copy.getData());
    }

    /**
     * Tests adding and retrieving extra data
     */
    @Test
    void testExtraData() {
        Message message = new Message(MessageType.PLAYER_JOIN, 1, "TestPlayer");
        
        message.addExtraData("key1", "value1");
        message.addExtraData("key2", 123);
        
        assertEquals("value1", message.getData().get("key1"));
        assertEquals(123, message.getData().get("key2"));
    }

    /**
     * Tests message serialization to string
     */
    @Test
    void testToString() {
        Message message = new Message(MessageType.PLAYER_JOIN, 1, "TestPlayer", true);
        message.setMessageId(123456L);
        message.addExtraData("key1", "value1");
        
        String messageString = message.toString();
        
        assertTrue(messageString.contains("PLAYER_JOIN"));
        assertTrue(messageString.contains("TestPlayer"));
        assertTrue(messageString.contains("true"));
        assertTrue(messageString.contains("key1=value1"));
    }

    /**
     * Tests message deserialization from string
     */
    @Test
    void testFromString() {
        String messageStr = "123456|PLAYER_JOIN|1|TestPlayer||true|key1=value1|";
        
        Message message = Message.fromString(messageStr);
        
        assertEquals(123456L, message.getMessageId());
        assertEquals(MessageType.PLAYER_JOIN, message.getType());
        assertEquals(1, message.getRoomId());
        assertEquals("TestPlayer", message.getFrom());
        assertTrue(message.isAck());
        assertEquals("value1", message.getData().get("key1"));
    }

    /**
     * Tests handling of invalid message format
     */
    @Test
    void testFromStringWithInvalidFormat() {
        String invalidMessage = "invalid|format";
        
        assertThrows(IllegalArgumentException.class, () -> Message.fromString(invalidMessage));
    }

    /**
     * Tests handling of null fields in message
     */
    @Test
    void testMessageWithNullFields() {
        Message message = new Message(MessageType.PLAYER_JOIN, 1, null, null);
        message.setMessageId(123456L);

        assertTrue(message.getFrom() == null || message.getFrom().isEmpty());
        assertTrue(message.getTo() == null || message.getTo().isEmpty());

        String expectedStr = "123456|PLAYER_JOIN|1|||false|";
        assertEquals(expectedStr, message.toString());

        Message parsedMessage = Message.fromString(expectedStr);
        assertEquals(MessageType.PLAYER_JOIN, parsedMessage.getType());
        assertEquals(1, parsedMessage.getRoomId());
        assertTrue(parsedMessage.getFrom() == null || parsedMessage.getFrom().isEmpty());
        assertTrue(parsedMessage.getTo() == null || parsedMessage.getTo().isEmpty());
        assertFalse(parsedMessage.isAck());
    }

    /**
     * Tests handling of empty string fields in message
     */
    @Test
    void testMessageWithEmptyFields() {
        Message message = new Message(MessageType.PLAYER_JOIN, 1, "", "");
        message.setMessageId(123456L);

        String expectedStr = "123456|PLAYER_JOIN|1|||false|";
        assertEquals(expectedStr, message.toString());

        Message parsedMessage = Message.fromString(expectedStr);
        assertEquals(MessageType.PLAYER_JOIN, parsedMessage.getType());
        assertEquals(1, parsedMessage.getRoomId());
        assertTrue(parsedMessage.getFrom() == null || parsedMessage.getFrom().isEmpty());
        assertTrue(parsedMessage.getTo() == null || parsedMessage.getTo().isEmpty());
        assertFalse(parsedMessage.isAck());
    }

    /**
     * Tests default constructor and field initialization
     */
    @Test
    void testDefaultConstructor() {
        Message message = new Message();
        message.setMessageId(123456L);
        message.setType(MessageType.PLAYER_JOIN);
        message.setRoomId(1);
        message.setFrom(null);
        message.setTo(null);
        message.setIsAck(false);

        String expectedStr = "123456|PLAYER_JOIN|1|||false|";
        assertEquals(expectedStr, message.toString());

        Message parsedMessage = Message.fromString(expectedStr);
        assertEquals(123456L, parsedMessage.getMessageId());
        assertEquals(MessageType.PLAYER_JOIN, parsedMessage.getType());
        assertEquals(1, parsedMessage.getRoomId());
        assertTrue(parsedMessage.getFrom() == null || parsedMessage.getFrom().isEmpty());
        assertTrue(parsedMessage.getTo() == null || parsedMessage.getTo().isEmpty());
        assertFalse(parsedMessage.isAck());
    }

    /**
     * Tests message with acknowledgment flag set
     */
    @Test
    void testMessageWithAckFlag() {
        Message message = new Message(MessageType.PLAYER_JOIN, 1, "TestPlayer", true);
        message.setMessageId(123456L);

        String expectedStr = "123456|PLAYER_JOIN|1|TestPlayer||true|";
        assertEquals(expectedStr, message.toString());

        Message parsedMessage = Message.fromString(expectedStr);
        assertEquals(MessageType.PLAYER_JOIN, parsedMessage.getType());
        assertEquals(1, parsedMessage.getRoomId());
        assertEquals("TestPlayer", parsedMessage.getFrom());
        assertTrue(parsedMessage.getTo() == null || parsedMessage.getTo().isEmpty());
        assertTrue(parsedMessage.isAck());
    }

    /**
     * Tests message deserialization with empty fields
     */
    @Test
    void testFromStringWithEmptyFields() {
        String messageStr = "123456|PLAYER_JOIN|1||||";
        Message message = Message.fromString(messageStr);

        assertEquals(123456L, message.getMessageId());
        assertEquals(MessageType.PLAYER_JOIN, message.getType());
        assertEquals(1, message.getRoomId());
        assertTrue(message.getFrom() == null || message.getFrom().isEmpty());
        assertTrue(message.getTo() == null || message.getTo().isEmpty());
        assertFalse(message.isAck());
    }
} 