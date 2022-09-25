package com.cryptoalgo.codable.preferencesCoder;

import com.cryptoalgo.codable.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Random;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

class CodableTest extends Codable<CodableTest.CodingKeys> {
    // These are public to facilitate testing
    private String str;
    private Boolean bool;
    private Integer integer;

    public CodableTest(String str, Boolean bool, Integer integer) {
        super(null);
        this.str = str;
        this.bool = bool;
        this.integer = integer;
    }

    protected CodableTest(Decoder<CodingKeys> decoder) throws DecodingException, NoSuchElementException {
        super(decoder);
        KeyedDecodingContainer<CodingKeys> container = decoder.container();
        integer = container.decodeInteger(CodingKeys.testInt);
        bool = container.decodeBoolean(CodingKeys.testBool);
        str = container.decodeString(CodingKeys.testString);
    }

    String getString() { return str; }
    Boolean getBoolean() { return bool; }
    Integer getInteger() { return integer; }

    public enum CodingKeys {
        testString, testBool, testInt
    }

    @Override
    public void encode(Encoder<CodingKeys> encoder) throws EncodingException {
        KeyedEncodingContainer<CodingKeys> container = encoder.container();
        container.encode(str, CodingKeys.testString);
        container.encode(bool, CodingKeys.testBool);
        container.encode(integer, CodingKeys.testInt);
    }
}

public class PreferencesTest {
    static private final String TESTING_PREF_NODE = "PreferencesUnitTest";

    @Test
    void testRoundTrip() throws Exception {
        CodableTest original = new CodableTest(
            UUID.randomUUID().toString(),
            true,
            new Random().nextInt()
        );
        new PreferencesEncoder(TESTING_PREF_NODE).encode(original);
        CodableTest decoded =
            (CodableTest) new PreferencesDecoder(TESTING_PREF_NODE).decode(CodableTest.class);

        assertEquals(decoded.getString(), original.getString());
        assertEquals(decoded.getBoolean(), original.getBoolean());
        assertEquals(decoded.getInteger(), original.getInteger());
    }

    @AfterAll
    static void cleanup() throws BackingStoreException {
        // Remove Preferences node to ensure clean state
        Preferences.userRoot().node(TESTING_PREF_NODE).removeNode();
    }
}