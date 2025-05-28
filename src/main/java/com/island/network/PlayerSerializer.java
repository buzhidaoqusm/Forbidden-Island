package com.island.network;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.island.model.Player;

import java.io.IOException;

public class PlayerSerializer extends StdSerializer<Player> {
    
    public PlayerSerializer() {
        this(null);
    }

    public PlayerSerializer(Class<Player> t) {
        super(t);
    }

    @Override
    public void serialize(Player player, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("id", player.getId());
        gen.writeStringField("name", player.getName());
        gen.writeStringField("role", player.getRole().name());
        gen.writeBooleanField("ready", player.isReady());
        gen.writeBooleanField("host", player.isHost());
        gen.writeBooleanField("inGame", player.isInGame());
        if (player.getPosition() != null) {
            gen.writeObjectFieldStart("position");
            gen.writeNumberField("x", player.getPosition().getX());
            gen.writeNumberField("y", player.getPosition().getY());
            gen.writeEndObject();
        }
        gen.writeEndObject();
    }
} 