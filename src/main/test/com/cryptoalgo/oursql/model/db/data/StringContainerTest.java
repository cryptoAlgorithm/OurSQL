package com.cryptoalgo.oursql.model.db.data;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StringContainerTest {
    @Test
    void unbox() {
        assertEquals("hello", new StringContainer("hello").getValue());
        final var random = UUID.randomUUID().toString();
        assertEquals(random, new StringContainer(random).getValue());
        assertEquals("null", new StringContainer("null").getValue());
        assertNull(new StringContainer(null).getValue());
    }

    @Test
    void isValid() {
        // Everything should be valid
        final var c = new StringContainer("hello");
        assertTrue(c.isValid("hello"));
        assertTrue(c.isValid("null"));
        assertTrue(c.isValid("123"));
        assertTrue(c.isValid("abc"));
        assertTrue(c.isValid(UUID.randomUUID().toString()));
    }

    @Test
    void testToString() {
        assertEquals("hello", new StringContainer("hello").toString());
        assertEquals("null", new StringContainer("null").toString());
        assertNull(new StringContainer(null).toString());
    }

    @Test
    void testToSQLString() {
        assertEquals("'hello'", new StringContainer("hello").toSQLString());
        assertEquals("'null'", new StringContainer("null").toSQLString());
        // Test escaping
        assertEquals("'you''re funny'", new StringContainer("you're funny").toSQLString());
        assertEquals("null", new StringContainer(null).toSQLString());
    }
}