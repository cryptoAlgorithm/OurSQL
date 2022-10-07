package com.cryptoalgo.oursql.model.db.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntNumberContainerTest {
    @Test
    void unbox() {
        var container = new IntNumberContainer("123");
        assertEquals(123, container.getValue());
        container = new IntNumberContainer("-123");
        assertEquals(-123, container.getValue());
        assertThrows(NumberFormatException.class, () -> new IntNumberContainer("null"));
    }

    @Test
    void getMin() {
        assertEquals(Integer.MIN_VALUE, new IntNumberContainer("123").getMin().intValue());
    }

    @Test
    void getMax() {
        assertEquals(Integer.MAX_VALUE, new IntNumberContainer("123").getMax().intValue());
    }

    @Test
    void getBigDecimalValue() {
        assertEquals(123, new IntNumberContainer("123").getBigDecimalValue(123).intValue());
    }

    @Test
    void isValid() {
        final var container = new IntNumberContainer("123");
        assertTrue(container.isValid("123"));
        assertTrue(container.isValid("-123"));
        assertTrue(container.isValid("1234567890"));
        assertTrue(container.isValid("-1234567890"));
        assertTrue(container.isValid("0"));
        assertTrue(container.isValid("-0"));
        assertTrue(container.isValid("")); // Empty strings are also valid
        assertTrue(container.isValid("-000000")); // Trailing 0s are valid
        assertTrue(container.isValid("000000"));
        // Anything else isn't
        assertTrue(container.isValid("999999999")); // Out of bounds
        assertTrue(container.isValid("-999999999"));
        assertFalse(container.isValid("aaa"));
        assertFalse(container.isValid("123C"));
        assertFalse(container.isValid("123.123"));
        assertFalse(container.isValid("."));
        assertFalse(container.isValid("-."));
        assertFalse(container.isValid("123E"));
    }

    @Test
    void testToString() {
        var container = new IntNumberContainer("123");
        assertEquals("123", container.toString());
        container = new IntNumberContainer("-123");
        assertEquals("-123", container.toString());
        assertThrows(NumberFormatException.class, () -> new IntNumberContainer("null"));
        assertThrows(NumberFormatException.class, () -> new IntNumberContainer("whatever"));
    }
}