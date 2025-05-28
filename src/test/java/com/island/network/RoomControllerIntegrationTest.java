package com.island.network;

import com.island.controller.GameController;
import com.island.model.*;
import com.island.view.ActionLogView;
import com.island.view.GameView;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.net.SocketException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * RoomController的集成测试类
 * 测试RoomController与其他组件的集成
 */
@DisplayName("RoomController集成测试")
class RoomControllerIntegrationTest {

    private GameController gameController;
    private Room room;
    private ActionLogView logView;
    private RoomController roomController;
    private GameView gameView;
    
    // 测试常量
    private static final long NETWORK_TIMEOUT = 30000; // 30秒
    private static final long WAIT_TIME = 100; // 100毫秒等待时间

    @BeforeEach
    void setUp() throws SocketException {
        // 创建普通mock对象，不使用stubOnly
        logView = mock(ActionLogView.class);
        gameView = mock(GameView.class);
        
        // 初始化房间
        room = new Room();
        room.setRoomId("test-room-" + System.currentTimeMillis());
        
        // 初始化游戏控制器
        gameController = new GameController(null);
        gameController.setGameView(gameView);
        
        // 创建RoomController
        roomController = new RoomController(gameController, room, logView);
        gameController.setRoomController(roomController);
        
        // 启动RoomController
        roomController.start();
    }

    @Test
    @DisplayName("测试完整的玩家加入流程")
    void testCompletePlayerJoinFlow() throws InterruptedException {
        // 创建玩家
        Player player = new Messenger("testPlayer");
        player.setReady(true);
        
        // 创建加入消息
        Message joinMessage = new Message(MessageType.PLAYER_JOIN, room.getRoomId(), player.getName())
            .addExtraData("username", player);
        
        // 处理加入请求
        roomController.handleJoinRequest(joinMessage);
        
        // 等待处理完成
        Thread.sleep(WAIT_TIME);
        
        // 验证玩家是否成功加入
        assertTrue(room.getPlayers().contains(player));
        assertEquals(player, room.getHostPlayer());
        assertTrue(player.isHost());
        verify(logView, atLeastOnce()).success(anyString());
    }

    @Test
    @DisplayName("测试游戏启动流程")
    void testGameStartFlow() throws InterruptedException {
        // 添加测试玩家
        Player player = new Messenger("startTestPlayer");
        player.setReady(true);
        room.addPlayer(player);
        
        // 设置玩家为房主
        room.setHostPlayer(player);
        player.setHost(true);
        
        // 创建游戏启动消息
        Message startMessage = new Message(MessageType.GAME_START, room.getRoomId(), "system")
            .addExtraData("username", player.getName());
        
        // 发送游戏启动消息
        roomController.sendGameStartMessage(startMessage);
        
        // 等待游戏启动
        Thread.sleep(WAIT_TIME);
        
        // 验证游戏状态
        assertTrue(gameController.isGameStart());
        assertNotNull(gameController.getCurrentPlayer());
    }

    @Test
    @DisplayName("测试游戏状态同步")
    void testGameStateSynchronization() throws InterruptedException {
        // 添加两个玩家
        Player player1 = new Messenger("syncTestPlayer1");
        Player player2 = new Engineer("syncTestPlayer2");
        
        player1.setReady(true);
        player2.setReady(true);
        
        room.addPlayer(player1);
        room.addPlayer(player2);
        
        // 设置房主
        room.setHostPlayer(player1);
        player1.setHost(true);
        
        // 验证玩家数量
        assertEquals(2, room.getPlayers().size());
        
        // 启动游戏
        Message startMessage = new Message(MessageType.GAME_START, room.getRoomId(), "system")
            .addExtraData("username", player1.getName());
        roomController.sendGameStartMessage(startMessage);
        
        // 等待同步
        Thread.sleep(WAIT_TIME);
        
        // 验证游戏状态
        assertTrue(gameController.isGameStart());
        assertNotNull(gameController.getCurrentPlayer());
        assertEquals(GameState.RUNNING, gameController.getGameSubject().getGameState());
    }

    @AfterEach
    void tearDown() {
        if (roomController != null) {
            roomController.shutdown();
        }
    }
} 