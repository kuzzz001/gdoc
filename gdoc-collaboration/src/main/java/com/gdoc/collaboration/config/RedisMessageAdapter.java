package com.gdoc.collaboration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RedisMessageAdapter implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(RedisMessageAdapter.class);

    private final ObjectMapper objectMapper;

    public RedisMessageAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody());
            String channel = new String(message.getChannel());
            Map<String, Object> event = objectMapper.readValue(body, Map.class);
            log.debug("Received Redis message on channel {}: {}", channel, event.get("type"));
        } catch (Exception e) {
            log.error("Failed to process Redis message: {}", e.getMessage());
        }
    }
}