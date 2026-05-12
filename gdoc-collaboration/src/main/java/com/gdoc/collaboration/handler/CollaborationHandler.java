package com.gdoc.collaboration.handler;

import com.gdoc.collaboration.entity.Room;
import com.gdoc.collaboration.ot.Operation;
import com.gdoc.collaboration.ot.OperationBuffer;
import com.gdoc.collaboration.service.RoomManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class CollaborationHandler {

    private static final Logger log = LoggerFactory.getLogger(CollaborationHandler.class);

    private final RoomManager roomManager;
    private final SimpMessagingTemplate messagingTemplate;

    public CollaborationHandler(RoomManager roomManager, SimpMessagingTemplate messagingTemplate) {
        this.roomManager = roomManager;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/doc/{docId}/join")
    public void joinRoom(@DestinationVariable Long docId,
                         SimpMessageHeaderAccessor headerAccessor,
                         Principal principal) {
        String sessionId = headerAccessor.getSessionId();
        Map<String, Object> attributes = headerAccessor.getSessionAttributes();

        Long userId = (Long) attributes.get("userId");
        String username = (String) attributes.get("username");

        if (userId == null) {
            log.warn("No user info in session for sessionId: {}", sessionId);
            return;
        }

        Room room = roomManager.getOrCreateRoom(docId);
        roomManager.addMember(docId, sessionId, userId, username);

        Room.RoomMember member = room.getMembers().get(sessionId);

        Map<String, Object> joinEvent = new HashMap<>();
        joinEvent.put("type", "user_joined");
        joinEvent.put("userId", userId);
        joinEvent.put("username", username);
        joinEvent.put("memberCount", room.getMembers().size());
        joinEvent.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/doc/" + docId, joinEvent);

        Map<String, Object> syncEvent = new HashMap<>();
        syncEvent.put("type", "full_sync");
        syncEvent.put("content", room.getContent());
        syncEvent.put("version", room.getVersion());
        syncEvent.put("members", room.getMembers().values().stream().map(m -> {
            Map<String, Object> memberInfo = new HashMap<>();
            memberInfo.put("userId", m.getUserId());
            memberInfo.put("username", m.getUsername());
            memberInfo.put("joinedAt", m.getJoinedAt().toString());
            return memberInfo;
        }).toList());

        messagingTemplate.convertAndSendToUser(sessionId, "/queue/sync", syncEvent);

        log.info("User {} joined document room {} with session {}", username, docId, sessionId);
    }

    @MessageMapping("/doc/{docId}/leave")
    public void leaveRoom(@DestinationVariable Long docId,
                          SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        Map<String, Object> attributes = headerAccessor.getSessionAttributes();

        Long userId = (Long) attributes.get("userId");
        String username = (String) attributes.get("username");

        Room room = roomManager.getRoom(docId);
        if (room != null) {
            Room.RoomMember member = room.getMembers().get(sessionId);
            if (member != null) {
                roomManager.removeMember(docId, sessionId);

                Map<String, Object> leaveEvent = new HashMap<>();
                leaveEvent.put("type", "user_left");
                leaveEvent.put("userId", userId);
                leaveEvent.put("username", username);
                leaveEvent.put("memberCount", room.getMembers().size());
                leaveEvent.put("timestamp", LocalDateTime.now().toString());

                messagingTemplate.convertAndSend("/topic/doc/" + docId, leaveEvent);
            }
        }

        log.info("User {} left document room {}", username, docId);
    }

    @MessageMapping("/doc/{docId}/operation")
    public void handleOperation(@DestinationVariable Long docId,
                                SimpMessageHeaderAccessor headerAccessor,
                                @Payload Map<String, Object> payload) {
        String sessionId = headerAccessor.getSessionId();
        Map<String, Object> attributes = headerAccessor.getSessionAttributes();

        Long userId = (Long) attributes.get("userId");
        String username = (String) attributes.get("username");

        Room room = roomManager.getRoom(docId);
        if (room == null) {
            log.warn("Room {} not found for operation", docId);
            return;
        }

        if (!room.hasMember(sessionId)) {
            log.warn("Session {} not in room {}", sessionId, docId);
            return;
        }

        List<Map<String, Object>> opsData = (List<Map<String, Object>>) payload.get("operations");
        Integer clientVersion = (Integer) payload.get("version");

        if (opsData == null || opsData.isEmpty()) {
            log.warn("No operations in payload");
            return;
        }

        OperationBuffer clientOps = new OperationBuffer();
        for (Map<String, Object> opData : opsData) {
            String opType = (String) opData.get("type");
            int count = opData.get("count") != null ? ((Number) opData.get("count")).intValue() : 0;
            String text = (String) opData.get("text");

            Operation op = switch (opType.toLowerCase()) {
                case "insert" -> Operation.insert(text != null ? text : "");
                case "delete" -> Operation.delete(count);
                case "retain" -> Operation.retain(count);
                default -> null;
            };
            if (op != null) {
                clientOps.add(op);
            }
        }

        int newClientVersion = clientVersion != null ? clientVersion : room.getVersion();
        OperationBuffer appliedOps = room.applyOperation(clientOps, newClientVersion);

        Map<String, Object> operationEvent = new HashMap<>();
        operationEvent.put("type", "operation");
        operationEvent.put("userId", userId);
        operationEvent.put("username", username);
        operationEvent.put("version", room.getVersion());
        operationEvent.put("operations", opsData);

        messagingTemplate.convertAndSend("/topic/doc/" + docId, operationEvent);

        Map<String, Object> ackEvent = new HashMap<>();
        ackEvent.put("type", "ack");
        ackEvent.put("version", room.getVersion());
        messagingTemplate.convertAndSendToUser(sessionId, "/queue/sync", ackEvent);

        log.debug("Operation applied in room {} by user {}, new version: {}", docId, username, room.getVersion());
    }

    @MessageMapping("/doc/{docId}/cursor")
    public void handleCursor(@DestinationVariable Long docId,
                            SimpMessageHeaderAccessor headerAccessor,
                            @Payload Map<String, Object> payload) {
        String sessionId = headerAccessor.getSessionId();
        Map<String, Object> attributes = headerAccessor.getSessionAttributes();

        Long userId = (Long) attributes.get("userId");
        String username = (String) attributes.get("username");

        Room room = roomManager.getRoom(docId);
        if (room == null || !room.hasMember(sessionId)) {
            return;
        }

        Long cursorPosition = payload.get("cursorPosition") != null ?
                ((Number) payload.get("cursorPosition")).longValue() : 0L;
        Long cursorEnd = payload.get("cursorEnd") != null ?
                ((Number) payload.get("cursorEnd")).longValue() : cursorPosition;

        Room.RoomMember member = room.getMembers().get(sessionId);
        if (member != null) {
            member.setCursorPosition(cursorPosition);
            member.setCursorEnd(cursorEnd);
        }

        Map<String, Object> cursorEvent = new HashMap<>();
        cursorEvent.put("type", "cursor_update");
        cursorEvent.put("userId", userId);
        cursorEvent.put("username", username);
        cursorEvent.put("cursorPosition", cursorPosition);
        cursorEvent.put("cursorEnd", cursorEnd);
        cursorEvent.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/doc/" + docId, cursorEvent);
    }
}
