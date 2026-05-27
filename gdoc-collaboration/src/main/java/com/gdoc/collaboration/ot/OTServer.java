package com.gdoc.collaboration.ot;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class OTServer {

    private final Map<Long, DocumentRoom> rooms = new ConcurrentHashMap<>();

    public DocumentRoom getRoom(Long docId) {
        return rooms.computeIfAbsent(docId, DocumentRoom::new);
    }

    public void removeRoom(Long docId) {
        rooms.remove(docId);
    }

    public static class DocumentRoom {
        private final Long docId;
        private String content;
        private int version;
        private final List<Delta> history = new ArrayList<>();
        private final Map<Long, SelectionState> selections = new ConcurrentHashMap<>();
        private final Set<Long> onlineUsers = ConcurrentHashMap.newKeySet();

        public DocumentRoom(Long docId) {
            this.docId = docId;
            this.content = "";
            this.version = 0;
        }

        public synchronized OTResult applyDelta(Long userId, Delta clientDelta, int clientVersion) {
            // Transform against concurrent operations
            Delta transformedDelta = clientDelta;
            for (int i = clientVersion; i < history.size(); i++) {
                Delta serverOp = history.get(i);
                DeltaTransform.OTTransformResult result = DeltaTransform.transform(serverOp, transformedDelta);
                transformedDelta = result.rightPrime();
            }

            // Apply to document
            content = DeltaApply.apply(content, transformedDelta);
            version++;
            history.add(transformedDelta);

            // Transform selections
            for (Map.Entry<Long, SelectionState> entry : selections.entrySet()) {
                if (!entry.getKey().equals(userId)) {
                    entry.setValue(entry.getValue().transform(transformedDelta, false));
                }
            }

            return new OTResult(transformedDelta, version);
        }

        public void updateSelection(Long userId, SelectionState selection) {
            selections.put(userId, selection);
        }

        public void removeSelection(Long userId) {
            selections.remove(userId);
            onlineUsers.remove(userId);
        }

        public void addOnlineUser(Long userId) {
            onlineUsers.add(userId);
        }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public int getVersion() { return version; }
        public void setVersion(int version) { this.version = version; }
        public Map<Long, SelectionState> getSelections() { return selections; }
        public Set<Long> getOnlineUsers() { return onlineUsers; }
    }

    public record OTResult(Delta delta, int version) {}

    public static class DeltaTransform {
        public record OTTransformResult(Delta leftPrime, Delta rightPrime) {}

        public static OTTransformResult transform(Delta left, Delta right) {
            // Use the Delta's built-in transform
            Delta leftPrime = right.transform(left, true);
            Delta rightPrime = left.transform(right, false);
            return new OTTransformResult(leftPrime, rightPrime);
        }
    }

    public static class DeltaApply {
        public static String apply(String doc, Delta delta) {
            if (doc == null) doc = "";
            StringBuilder sb = new StringBuilder(doc);
            int pos = 0;

            for (Delta.DeltaOp op : delta.getOps()) {
                switch (op.getType()) {
                    case RETAIN -> pos += op.getCount();
                    case INSERT -> {
                        sb.insert(pos, op.getText());
                        pos += op.getText().length();
                    }
                    case DELETE -> {
                        int end = Math.min(pos + op.getCount(), sb.length());
                        sb.delete(pos, end);
                    }
                }
            }
            return sb.toString();
        }
    }
}