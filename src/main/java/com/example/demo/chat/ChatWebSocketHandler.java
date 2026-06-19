package com.example.demo.chat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final Map<String, Participant> participants = new ConcurrentHashMap<>();
    private final Map<String, CopyOnWriteArrayList<ChatMessage>> histories = new ConcurrentHashMap<>();
    private final Map<String, String> customerNames = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> payload = objectMapper.readValue(
                message.getPayload(), new TypeReference<>() {});
        String type = text(payload.get("type"));

        if ("JOIN".equals(type)) {
            join(session, payload);
        } else if ("MESSAGE".equals(type)) {
            sendMessage(session, payload);
        }
    }

    private void join(WebSocketSession session, Map<String, Object> payload) throws IOException {
        String role = text(payload.get("role"));
        String userId = text(payload.get("userId"));
        String name = text(payload.get("name"));

        if (userId.isBlank() || (!"CUSTOMER".equals(role) && !"ADMIN".equals(role))) {
            send(session, Map.of("type", "ERROR", "message", "Thông tin kết nối không hợp lệ"));
            return;
        }

        Participant participant = new Participant(role, userId, name.isBlank() ? "Khách hàng" : name);
        participants.put(session.getId(), participant);

        if ("ADMIN".equals(role)) {
            sendSnapshot(session);
        } else {
            customerNames.put(userId, participant.name());
            send(session, Map.of(
                    "type", "HISTORY",
                    "conversationId", userId,
                    "messages", histories.getOrDefault(userId, new CopyOnWriteArrayList<>())
            ));
            broadcastSnapshotToAdmins();
        }
    }

    private void sendMessage(WebSocketSession session, Map<String, Object> payload) throws IOException {
        Participant sender = participants.get(session.getId());
        if (sender == null) {
            send(session, Map.of("type", "ERROR", "message", "Vui lòng kết nối lại"));
            return;
        }

        String content = text(payload.get("content")).trim();
        if (content.isBlank() || content.length() > 1000) {
            send(session, Map.of("type", "ERROR", "message", "Tin nhắn phải từ 1 đến 1000 ký tự"));
            return;
        }

        String conversationId = "CUSTOMER".equals(sender.role())
                ? sender.userId()
                : text(payload.get("conversationId"));
        if (conversationId.isBlank()) {
            send(session, Map.of("type", "ERROR", "message", "Chưa chọn khách hàng"));
            return;
        }

        ChatMessage chatMessage = new ChatMessage(
                System.nanoTime(),
                conversationId,
                sender.role(),
                sender.name(),
                content,
                LocalDateTime.now().toString()
        );
        histories.computeIfAbsent(conversationId, key -> new CopyOnWriteArrayList<>()).add(chatMessage);

        Map<String, Object> event = Map.of("type", "MESSAGE", "message", chatMessage);
        for (WebSocketSession target : openSessions()) {
            Participant receiver = participants.get(target.getId());
            if (receiver == null) continue;
            if ("ADMIN".equals(receiver.role()) || conversationId.equals(receiver.userId())) {
                send(target, event);
            }
        }
        broadcastSnapshotToAdmins();
    }

    private void sendSnapshot(WebSocketSession session) throws IOException {
        List<Map<String, Object>> conversations = new ArrayList<>();
        histories.forEach((customerId, messages) -> {
            ChatMessage lastMessage = messages.isEmpty() ? null : messages.get(messages.size() - 1);
            conversations.add(Map.of(
                    "customerId", customerId,
                    "customerName", customerNames.getOrDefault(customerId, "Khách hàng #" + customerId),
                    "messages", messages,
                    "lastMessageAt", lastMessage == null ? "" : lastMessage.sentAt()
            ));
        });
        conversations.sort(Comparator.comparing(
                item -> text(item.get("lastMessageAt")), Comparator.reverseOrder()));
        send(session, Map.of("type", "SNAPSHOT", "conversations", conversations));
    }

    private void broadcastSnapshotToAdmins() {
        for (WebSocketSession session : openSessions()) {
            Participant participant = participants.get(session.getId());
            if (participant != null && "ADMIN".equals(participant.role())) {
                try {
                    sendSnapshot(session);
                } catch (IOException ignored) {

                }
            }
        }
    }

    private List<WebSocketSession> openSessions() {
        return participants.keySet().stream()
                .map(this::findSession)
                .filter(session -> session != null && session.isOpen())
                .toList();
    }

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    private WebSocketSession findSession(String sessionId) {
        return sessions.get(sessionId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        participants.remove(session.getId());
    }

    private void send(WebSocketSession session, Object value) throws IOException {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(value)));
        }
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private record Participant(String role, String userId, String name) {}

    public record ChatMessage(
            long id,
            String conversationId,
            String senderRole,
            String senderName,
            String content,
            String sentAt
    ) {}
}
