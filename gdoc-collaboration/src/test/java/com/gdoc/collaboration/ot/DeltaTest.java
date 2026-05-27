package com.gdoc.collaboration.ot;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DeltaTest {

    @Test
    void shouldInsertText() {
        Delta delta = new Delta();
        delta.insert("Hello");
        assertEquals(1, delta.getOps().size());
        assertEquals(Delta.DeltaOpType.INSERT, delta.getOps().get(0).getType());
        assertEquals("Hello", delta.getOps().get(0).getText());
    }

    @Test
    void shouldRetainWithAttributes() {
        Delta delta = new Delta();
        delta.retain(5, Map.of("bold", true));
        assertEquals(1, delta.getOps().size());
        assertEquals(Delta.DeltaOpType.RETAIN, delta.getOps().get(0).getType());
        assertEquals(5, delta.getOps().get(0).getCount());
        assertNotNull(delta.getOps().get(0).getAttributes());
    }

    @Test
    void shouldDeleteText() {
        Delta delta = new Delta();
        delta.delete(3);
        assertEquals(1, delta.getOps().size());
        assertEquals(Delta.DeltaOpType.DELETE, delta.getOps().get(0).getType());
        assertEquals(3, delta.getOps().get(0).getCount());
    }

    @Test
    void shouldComposeDeltas() {
        Delta d1 = new Delta();
        d1.insert("Hello");

        Delta d2 = new Delta();
        d2.retain(5);
        d2.insert(" World");

        Delta result = d1.compose(d2);
        assertNotNull(result);
    }

    @Test
    void shouldSerializeToJson() {
        Delta delta = new Delta();
        delta.insert("test", Map.of("bold", true));
        delta.retain(3);
        delta.delete(2);

        Map<String, Object> json = delta.toJson();
        assertNotNull(json);
        assertNotNull(json.get("ops"));
    }

    @Test
    void shouldDeserializeFromJson() {
        Delta delta = new Delta();
        delta.insert("test");
        Map<String, Object> json = delta.toJson();

        Delta restored = Delta.fromJson(json);
        assertNotNull(restored);
        assertEquals(1, restored.getOps().size());
    }
}