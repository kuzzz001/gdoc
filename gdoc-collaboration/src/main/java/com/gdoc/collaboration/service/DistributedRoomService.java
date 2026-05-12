package com.gdoc.collaboration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdoc.collaboration.config.DistributedLock;
import com.gdoc.collaboration.config.RedisPubSubConfig;
import com.gdoc.collaboration.entity.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DistributedRoomService {

    private static final Logger log = LoggerFactory.getLogger(DistributedRoomService.class);
    private static final String ROOM_KEY_PREFIX = "gdoc:room:";
    private static final String MEMBER_KEY_PREFIX = "gdoc:members:";
    private static final Duration ROOM_TTL = Duration.ofHours(24);

    private final RedisTemplate<String, Object> redisTemplate;
    private final DistributedLock distributedLock;
    private final RedisPubSubConfig redisPubSubConfig;
    private final ObjectMapper objectMapper;

    private final Map<Long, Room> localRooms = new ConcurrentHashMap<>();

    public DistributedRoomService(RedisTemplate<String, Object> redisTemplate,
                                  DistributedLock distributedLock,
                                  RedisPubSubConfig redisPubSubConfig,
                                  ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.distributedLock = distributedLock;
        this.redisPubSubConfig = redisPubSubConfig;
        this.objectMapper = objectMapper;
    }

    public Room getOrCreateRoom(Long docId) {
        String roomKey = ROOM_KEY_PREFIX + docId;

        Object cached = redisTemplate.opsForValue().get(roomKey);
        if (cached instanceof Room) {
            return (Room) cached;
        }

        Room room = localRooms.computeIfAbsent(docId, id -> new Room(docId));

        redisTemplate.opsForValue().set(roomKey, room, ROOM_TTL);

        subscribeToRoom(docId);

        return room;
    }

    public void saveRoom(Long docId, Room room) {
        String roomKey = ROOM_KEY_PREFIX + docId;
        redisTemplate.opsForValue().set(roomKey, room, ROOM_TTL);
        localRooms.put(docId, room);
    }

    public void removeRoom(Long docId) {
        String roomKey = ROOM_KEY_PREFIX + docId;
        redisTemplate.delete(roomKey);
        localRooms.remove(docId);

        String memberKey = MEMBER_KEY_PREFIX + docId;
        redisTemplate.delete(memberKey);

        redisPubSubConfig.unsubscribe(docId);
        log.info("Removed room {} from cache", docId);
    }

    public void broadcastToRoom(Long docId, Map<String, Object> event) {
        redisPubSubConfig.publish(docId, event);
    }

    public void addMember(Long docId, String sessionId, Long userId, String username) {
        String memberKey = MEMBER_KEY_PREFIX + docId;
        String memberValue = sessionId + ":" + userId + ":" + username;

        redisTemplate.opsForSet().add(memberKey, memberValue);
        redisTemplate.expire(memberKey, ROOM_TTL);

        log.debug("Added member {} to room {}", username, docId);
    }

    public void removeMember(Long docId, String sessionId, Long userId, String username) {
        String memberKey = MEMBER_KEY_PREFIX + docId;
        String memberValue = sessionId + ":" + userId + ":" + username;

        redisTemplate.opsForSet().remove(memberKey, memberValue);

        log.debug("Removed member {} from room {}", username, docId);
    }

    public boolean tryAcquireLock(Long docId, long timeoutSeconds) {
        return distributedLock.tryLock(docId, timeoutSeconds);
    }

    public void releaseLock(Long docId) {
        distributedLock.unlock(docId);
    }

    private void subscribeToRoom(Long docId) {
        redisPubSubConfig.subscribe(docId, (message, pattern) -> {
            try {
                String body = new String(message.getBody());
                Map<String, Object> event = objectMapper.readValue(body, Map.class);
                handleDistributedEvent(docId, event);
            } catch (Exception e) {
                log.error("Failed to handle distributed event: {}", e.getMessage());
            }
        });
    }

    private void handleDistributedEvent(Long docId, Map<String, Object> event) {
        String type = (String) event.get("type");
        log.debug("Handling distributed event type {} for doc {}", type, docId);
    }

    public Room getLocalRoom(Long docId) {
        return localRooms.get(docId);
    }
}