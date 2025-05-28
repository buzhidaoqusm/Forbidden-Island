package com.island.network;

import com.island.controller.GameController;
import com.island.model.*;
import com.island.view.ActionLogView;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

/**
 * RoomController的单元测试类
 */
@DisplayName("RoomController单元测试")
class RoomControllerTest {

    @Mock
    private GameController gameController;

    @Mock
    private Room room;

    @Mock
    private ActionLogView logView;

    private RoomController roomController;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() throws SocketException {
        mocks = MockitoAnnotations.openMocks(this);
        
        // 设置基本的mock行为
        when(room.getRoomId()).thenReturn("test-room");
        when(room.getPlayers()).thenReturn(new ArrayList<>());
        
        roomController = new RoomController(gameController, room, logView);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
        if (roomController != null) {
            roomController.shutdown();
        }
    }

    @Test
    @DisplayName("测试玩家加入请求处理")
    void testHandleJoinRequest() {
        // 准备测试数据
        String playerName = "testPlayer";
        Player mockPlayer = mock(Player.class);
        when(mockPlayer.getName()).thenReturn(playerName);
        when(room.addPlayer(any(Player.class))).thenReturn(true);
        
        Message joinMessage = new Message(MessageType.PLAYER_JOIN, "test-room", playerName)
            .addExtraData("username", mockPlayer);

        // 执行测试
        roomController.handleJoinRequest(joinMessage);

        // 验证结果
        verify(room).addPlayer(any(Player.class));
        verify(logView, atLeastOnce()).success(anyString());
    }

    @Test
    @DisplayName("测试游戏开始消息处理")
    void testHandleGameStartMessage() {
        // 准备测试数据
        Player mockPlayer = mock(Player.class);
        when(mockPlayer.getName()).thenReturn("testPlayer");
        when(room.getPlayerByName(anyString())).thenReturn(mockPlayer);
        when(room.getPlayers()).thenReturn(List.of(mockPlayer));
        
        Message startMessage = new Message(MessageType.GAME_START, "test-room", "system")
            .addExtraData("username", "testPlayer");

        // 执行测试
        roomController.sendGameStartMessage(startMessage);

        // 验证结果
        verify(gameController).startGame(anyLong());
    }

    @Test
    @DisplayName("测试玩家移动消息处理")
    void testHandleMoveMessage() {
        // 准备测试数据
        Player mockPlayer = mock(Player.class);
        Position targetPos = new Position(1, 1);
        Message moveMessage = new Message(MessageType.MOVE_PLAYER, "test-room", "player")
            .addExtraData("username", mockPlayer)
            .addExtraData("position", targetPos);

        // 设置mock行为
        lenient().when(room.getPlayerByName(anyString())).thenReturn(mockPlayer);

        // 执行测试
        roomController.handleGameMessage(moveMessage);

        // 验证结果
        verify(logView, times(1)).log(contains("Processing message"));
    }

    @Test
    @DisplayName("测试广播消息")
    void testBroadcastMessage() {
        // 准备测试数据
        Message message = new Message(MessageType.UPDATE_ROOM, "test-room", "system");

        // 执行测试
        roomController.broadcast(message);

        // 验证结果
        verify(logView, atLeastOnce()).log(anyString());
    }

    @Test
    @DisplayName("测试玩家心跳更新")
    void testUpdatePlayerHeartbeat() {
        // 准备测试数据
        String playerName = "testPlayer";

        // 执行测试
        roomController.updatePlayerHeartbeat(playerName);

        // 验证结果 - 只需要验证方法被调用，因为updatePlayerHeartbeat只是更新内部状态
        verify(logView, never()).error(anyString());
    }

    @Test
    @DisplayName("测试错误处理")
    void testErrorHandling() {
        // 准备测试数据
        Message invalidMessage = new Message(MessageType.MOVE_PLAYER, "test-room", "player");
        
        // 设置mock行为，确保会抛出异常
        MessageHandler mockMessageHandler = mock(MessageHandler.class);
        doThrow(new RuntimeException("测试错误")).when(mockMessageHandler).handleMessage(any(Message.class));
        
        // 使用反射设置mockMessageHandler
        try {
            java.lang.reflect.Field messageHandlerField = RoomController.class.getDeclaredField("messageHandler");
            messageHandlerField.setAccessible(true);
            messageHandlerField.set(roomController, mockMessageHandler);
        } catch (Exception e) {
            fail("Failed to set mock message handler: " + e.getMessage());
        }

        // 执行测试
        roomController.handleGameMessage(invalidMessage);

        // 验证结果
        verify(logView, atLeastOnce()).error(contains("测试错误"));
    }
} 