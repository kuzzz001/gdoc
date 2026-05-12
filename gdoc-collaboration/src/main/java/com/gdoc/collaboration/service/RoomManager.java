package com.gdoc.collaboration.service;

import com.gdoc.collaboration.entity.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomManager {

    private static final Logger log = LoggerFactory.getLogger(RoomManager.class);

    private final Map<Long, Room> rooms = new ConcurrentHashMap<>();

    public Room getOrCreateRoom(Long docId) {
        return rooms.computeIfAbsent(docId, Room::new);
    }

    public Room getRoom(Long docId) {
        return rooms.get(docId);
    }

    public void addMember(Long docId, String sessionId, Long userId, String username) {
        Room room = getOrCreateRoom(docId);
        room.addMember(sessionId, userId, username);
        log.info("User {} joined room {}, total members: {}", username, docId, room.getMembers().size());
    }

    public void removeMember(Long docId, String sessionId) {
        Room room = getRoom(docId);
        if (room != null) {
            room.removeMember(sessionId);
            log.info("User left room {}, remaining members: {}", docId, room.getMembers().size());
            if (room.getMembers().isEmpty()) {
                rooms.remove(docId);
                log.info("Room {} is now empty and removed", docId);
            }
        }
    }

    public Collection<Room> getAllRooms() {
        return rooms.values();
    }

    public int getRoomCount() {
        return rooms.size();
    }
}