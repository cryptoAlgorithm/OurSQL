package com.cryptoalgo.oursql.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;

class SecretsStoreTest {
    static final String id = UUID.randomUUID().toString();

    @Test
    void cipherKitchenSink() throws Exception {
        String
            original = UUID.randomUUID().toString(),
            pw = UUID.randomUUID().toString();
        SecretsStore.encrypt(original, pw, id);
        assertTrue(SecretsStore.isEncrypted(id));
        assertEquals(
            SecretsStore.decrypt(pw, id),
            original
        );
    }

    @Test
    void plainTextStore() throws Exception {
        String original = UUID.randomUUID().toString();
        SecretsStore.encrypt(original, id);
        assertFalse(SecretsStore.isEncrypted(id));
        assertEquals(SecretsStore.decrypt(id), original);
    }

    @AfterEach
    void cleanup() throws Exception {
        Preferences.userNodeForPackage(SecretsStore.class).node(id).removeNode();
    }
}