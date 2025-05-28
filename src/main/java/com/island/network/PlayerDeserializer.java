package com.island.network;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.island.model.*;

import java.io.IOException;

public class PlayerDeserializer extends StdDeserializer<Player> {

    public PlayerDeserializer() {
        this(null);
    }

    public PlayerDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Player deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        
        String name = node.get("name").asText();
        String roleStr = node.get("role").asText();
        PlayerRole role = PlayerRole.valueOf(roleStr);
        
        // 根据角色创建对应的Player实例
        Player player = switch (role) {
            case EXPLORER -> new Explorer(name);
            case PILOT -> new Pilot(name);
            case NAVIGATOR -> new Navigator(name);
            case DIVER -> new Diver(name);
            case ENGINEER -> new Engineer(name);
            case MESSENGER -> new Messenger(name);
        };
        
        // 设置其他属性
        player.setReady(node.get("ready").asBoolean());
        player.setHost(node.get("host").asBoolean());
        player.setInGame(node.get("inGame").asBoolean());
        
        // 设置位置
        if (node.has("position")) {
            JsonNode posNode = node.get("position");
            int x = posNode.get("x").asInt();
            int y = posNode.get("y").asInt();
            player.setPosition(new Position(x, y));
        }
        
        return player;
    }
} 