package com.gdoc.collaboration.entity;

import com.gdoc.collaboration.ot.OperationBuffer;
import com.gdoc.collaboration.ot.OTEngine;
import com.gdoc.collaboration.ot.OTTransform;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Room {
    private Long docId;
    private volatile String content;
    private final AtomicInteger version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final Map<String, RoomMember> members = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<OperationBuffer> pendingOps = new ConcurrentLinkedQueue<>();
    private final Object lock = new Object();

    public Room(Long docId) {
        this.docId = docId;
        this.content = "";
        this.version = new AtomicInteger(0);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Room(Long docId, String initialContent) {
        this.docId = docId;
        this.content = initialContent != null ? initialContent : "";
        this.version = new AtomicInteger(0);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void addMember(String sessionId, Long userId, String username) {
        members.put(sessionId, new RoomMember(sessionId, userId, username));
        this.updatedAt = LocalDateTime.now();
    }

    public void removeMember(String sessionId) {
        members.remove(sessionId);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasMember(String sessionId) {
        return members.containsKey(sessionId);
    }

    public int getVersion() {
        return version.get();
    }

    public OperationBuffer applyOperation(OperationBuffer clientOps, int clientVersion) {
        synchronized (lock) {
            int currentVersion = version.get();

            while (clientVersion > currentVersion && !pendingOps.isEmpty()) {
                try {
                    lock.wait(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            OperationBuffer transformedOps = new OperationBuffer();
            OperationBuffer newOps = clientOps.clone();

            while (currentVersion < clientVersion && !pendingOps.isEmpty()) {
                OperationBuffer serverOps = pendingOps.poll();
                if (serverOps != null) {
                    OTTransform.TransformResult result = OTTransform.transform(newOps, serverOps);
                    newOps = result.leftPrime();
                }
                currentVersion++;
            }

            content = OTEngine.apply(content, newOps);
            version.incrementAndGet();
            pendingOps.offer(newOps);

            lock.notifyAll();

            return newOps;
        }
    }

    public void setContent(String newContent) {
        synchronized (lock) {
            this.content = newContent;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public String getContent() {
        return content;
    }

    public Long getDocId() { return docId; }
    public void setDocId(Long docId) { this.docId = docId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Map<String, RoomMember> getMembers() { return members; }

    public ConcurrentLinkedQueue<OperationBuffer> getPendingOps() { return pendingOps; }

    public Object getLock() { return lock; }

    public static class RoomMember {
        private String sessionId;
        private Long userId;
        private String username;
        private LocalDateTime joinedAt;
        private Long cursorPosition;
        private Long cursorEnd;

        public RoomMember(String sessionId, Long userId, String username) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.username = username;
            this.joinedAt = LocalDateTime.now();
            this.cursorPosition = 0L;
            this.cursorEnd = 0L;
        }

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public LocalDateTime getJoinedAt() { return joinedAt; }
        public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

        public Long getCursorPosition() { return cursorPosition; }
        public void setCursorPosition(Long cursorPosition) { this.cursorPosition = cursorPosition; }

        public Long getCursorEnd() { return cursorEnd; }
        public void setCursorEnd(Long cursorEnd) { this.cursorEnd = cursorEnd; }
    }
}