package com.cryptoalgo.oursql.model.db.data;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlaceholderContainerTest {
    @Test
    void unbox() {
        final var c = new PlaceholderContainer("a");
        assertNull(c.unbox("funny"));
        assertNull(c.unbox(UUID.randomUUID().toString())); // Should return null regardless
        // No-arg constructor should function the same
        assertNull(new PlaceholderContainer().unbox("abc1213213"));
    }

    @Test
    void isEditable() {
        assertFalse(new PlaceholderContainer().isEditable());
    }

    @Test
    void isValid() {
        // Should always return false
        assertFalse(new PlaceholderContainer().isValid(UUID.randomUUID().toString()));
    }

    @Test
    void testToString() {
        // Should always return an empty string ""
        final var c = new PlaceholderContainer("abc");
        assertEquals("", c.toString());
        assertEquals("", new PlaceholderContainer().toString());
        assertEquals("", new PlaceholderContainer(null).toString());
    }
}