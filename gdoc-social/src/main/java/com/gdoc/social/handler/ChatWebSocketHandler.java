package com.gdoc.social.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdoc.model.dto.MessageVO;
import com.gdoc.model.dto.SendMessageRequest;
import com.gdoc.social.service.MessageService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Map<Long, WebSocketSession> onlineUsers = new ConcurrentHashMap<>();
    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(MessageService messageService, ObjectMapper objectMapper) {
        this.messageService = messageService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = extractUserId(session);
        if (userId != null) {
            onlineUsers.put(userId, session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        Long userId = extractUserId(session);
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        JsonNode node = objectMapper.readTree(textMessage.getPayload());
        String type = node.has("type") ? node.get("type").asText() : "chat";

        switch (type) {
            case "chat" -> handleChatMessage(userId, node);
            case "read" -> handleReadMessage(userId, node);
            case "typing" -> handleTypingNotification(userId, node);
        }
    }

    private void handleChatMessage(Long senderId, JsonNode node) throws IOException {
        SendMessageRequest request = new SendMessageRequest();
        request.setReceiverId(node.get("receiverId").asLong());
        request.setContent(node.get("content").asText());
        if (node.has("msgType")) {
            request.setMsgType(node.get("msgType").asText());
        }
        if (node.has("fileUrl")) {
            request.setFileUrl(node.get("fileUrl").asText());
        }
        if (node.has("fileName")) {
            request.setFileName(node.get("fileName").asText());
        }
        if (node.has("fileSize")) {
            request.setFileSize(node.get("fileSize").asLong());
        }

        MessageVO message = messageService.sendMessage(senderId, request);

        String messageJson = objectMapper.writeValueAsString(Map.of(
                "type", "chat",
                "message", message
        ));

        WebSocketSession receiverSession = onlineUsers.get(request.getReceiverId());
        if (receiverSession != null && receiverSession.isOpen()) {
            receiverSession.sendMessage(new TextMessage(messageJson));
            messageService.markAsDelivered(request.getReceiverId(), senderId);
        }

        WebSocketSession senderSession = onlineUsers.get(senderId);
        if (senderSession != null && senderSession.isOpen()) {
            senderSession.sendMessage(new TextMessage(messageJson));
        }
    }

    private void handleReadMessage(Long receiverId, JsonNode node) throws IOException {
        Long senderId = node.get("senderId").asLong();
        messageService.markAsRead(receiverId, senderId);

        WebSocketSession senderSession = onlineUsers.get(senderId);
        if (senderSession != null && senderSession.isOpen()) {
            String readJson = objectMapper.writeValueAsString(Map.of(
                    "type", "read",
                    "readBy", receiverId
            ));
            senderSession.sendMessage(new TextMessage(readJson));
        }
    }

    private void handleTypingNotification(Long senderId, JsonNode node) throws IOException {
        Long receiverId = node.get("receiverId").asLong();
        WebSocketSession receiverSession = onlineUsers.get(receiverId);
        if (receiverSession != null && receiverSession.isOpen()) {
            String typingJson = objectMapper.writeValueAsString(Map.of(
                    "type", "typing",
                    "userId", senderId
            ));
            receiverSession.sendMessage(new TextMessage(typingJson));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = extractUserId(session);
        if (userId != null) {
            onlineUsers.remove(userId);
        }
    }

    private Long extractUserId(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=");
                if (kv.length == 2 && "userId".equals(kv[0])) {
                    return Long.parseLong(kv[1]);
                }
            }
        }
        return null;
    }

    public static boolean isOnline(Long userId) {
        WebSocketSession session = onlineUsers.get(userId);
        return session != null && session.isOpen();
    }
}
