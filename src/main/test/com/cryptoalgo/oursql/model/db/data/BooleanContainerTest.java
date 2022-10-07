package com.cryptoalgo.oursql.model.db.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BooleanContainerTest {
    @Test
    void testUnbox() {
        var container = new BooleanContainer("true");
        assertEquals(true, container.getValue());
        container = new BooleanContainer("false");
        assertEquals(false, container.getValue());
        container = new BooleanContainer("null");
        assertNull(container.getValue());
    }

    @Test
    void testGetFinalValue() {
        final var container = new BooleanContainer("true");
        assertEquals("true", container.getFinalValue("true"));
        assertEquals("false", container.getFinalValue("false"));
        assertNull(container.getFinalValue("abc"));
        assertNull(container.getFinalValue("true123"));
        assertNull(container.getFinalValue("tr"));
        assertNull(container.getFinalValue("null"));
    }

    @Test
    void testIsValid() {
        final var container = new BooleanContainer("true");
        assertTrue(container.isValid("true"));
        assertTrue(container.isValid("false"));
        assertTrue(container.isValid("fa")); // Partial strings are valid
        assertTrue(container.isValid("tr"));
        assertTrue(container.isValid("")); // Empty strings are also valid
        // Anything else isn't
        assertFalse(container.isValid("aaa"));
        assertFalse(container.isValid("falseC"));
        assertFalse(container.isValid("true123"));
        assertFalse(container.isValid("trueE"));
    }

    @Test
    void testToString() {
        var container = new BooleanContainer("true");
        assertEquals("true", container.toString());
        container = new BooleanContainer("false");
        assertEquals("false", container.toString());
        container = new BooleanContainer("null");
        assertNull(container.toString());
        container = new BooleanContainer("whatever");
        assertNull(container.toString());
    }

    @Test
    void testToSQLString() {
        var container = new BooleanContainer("true");
        assertEquals("true", container.toSQLString());
        container = new BooleanContainer("false");
        assertEquals("false", container.toSQLString());
        container = new BooleanContainer("null");
        assertEquals("null", container.toSQLString());
    }
}