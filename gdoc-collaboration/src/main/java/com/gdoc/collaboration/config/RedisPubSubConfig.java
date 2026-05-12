package com.gdoc.collaboration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RedisPubSubConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisPubSubConfig.class);
    private static final String DOC_CHANNEL_PREFIX = "gdoc:doc:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisMessageListenerContainer listenerContainer;
    private final ObjectMapper objectMapper;

    private final Map<String, MessageListener> listeners = new ConcurrentHashMap<>();

    public RedisPubSubConfig(RedisTemplate<String, Object> redisTemplate,
                             RedisMessageListenerContainer listenerContainer,
                             ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.listenerContainer = listenerContainer;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    public void publish(Long docId, Map<String, Object> message) {
        String channel = DOC_CHANNEL_PREFIX + docId;
        try {
            redisTemplate.convertAndSend(channel, message);
            log.debug("Published message to channel {}: {}", channel, message.get("type"));
        } catch (Exception e) {
            log.error("Failed to publish message to channel {}: {}", channel, e.getMessage());
        }
    }

    public void subscribe(Long docId, MessageListener listener) {
        String channel = DOC_CHANNEL_PREFIX + docId;
        if (listeners.containsKey(channel)) {
            log.debug("Already subscribed to channel: {}", channel);
            return;
        }

        ChannelTopic topic = new ChannelTopic(channel);
        listenerContainer.addMessageListener(listener, topic);
        listeners.put(channel, listener);
        log.info("Subscribed to channel: {}", channel);
    }

    public void unsubscribe(Long docId) {
        String channel = DOC_CHANNEL_PREFIX + docId;
        MessageListener listener = listeners.remove(channel);
        if (listener != null) {
            ChannelTopic topic = new ChannelTopic(channel);
            listenerContainer.removeMessageListener(listener, topic);
            log.info("Unsubscribed from channel: {}", channel);
        }
    }

    public String getChannelName(Long docId) {
        return DOC_CHANNEL_PREFIX + docId;
    }
}