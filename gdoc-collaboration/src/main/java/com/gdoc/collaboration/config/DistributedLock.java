package com.gdoc.collaboration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class DistributedLock {

    private static final Logger log = LoggerFactory.getLogger(DistributedLock.class);
    private static final String LOCK_PREFIX = "gdoc:lock:doc:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private final Map<String, Long> localLocks = new ConcurrentHashMap<>();

    public DistributedLock(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public boolean tryLock(Long docId, long timeoutSeconds) {
        String key = LOCK_PREFIX + docId;
        String instanceId = getInstanceId();

        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(key, instanceId, Duration.ofSeconds(timeoutSeconds));

        if (Boolean.TRUE.equals(acquired)) {
            localLocks.put(key, System.currentTimeMillis());
            log.debug("Acquired distributed lock for doc {}", docId);
            return true;
        }

        return false;
    }

    public void unlock(Long docId) {
        String key = LOCK_PREFIX + docId;
        String instanceId = getInstanceId();

        Object currentHolder = redisTemplate.opsForValue().get(key);
        if (instanceId.equals(currentHolder)) {
            redisTemplate.delete(key);
            localLocks.remove(key);
            log.debug("Released distributed lock for doc {}", docId);
        }
    }

    public boolean isLocked(Long docId) {
        String key = LOCK_PREFIX + docId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void extendLock(Long docId, long additionalSeconds) {
        String key = LOCK_PREFIX + docId;
        String instanceId = getInstanceId();

        Object currentHolder = redisTemplate.opsForValue().get(key);
        if (instanceId.equals(currentHolder)) {
            redisTemplate.expire(key, Duration.ofSeconds(additionalSeconds));
            log.debug("Extended lock for doc {} by {} seconds", docId, additionalSeconds);
        }
    }

    public long waitForLock(Long docId, long maxWaitSeconds) {
        long waited = 0;
        while (isLocked(docId) && waited < maxWaitSeconds) {
            try {
                TimeUnit.MILLISECONDS.sleep(50);
                waited += 0.05;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return waited;
    }

    private String getInstanceId() {
        return System.getProperty("spring.application.name", "gdoc-server") + "-" +
               java.net.InetAddress.getLoopbackAddress().getHostAddress() + "-" +
               ProcessHandle.current().pid();
    }
}