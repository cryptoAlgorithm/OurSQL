package com.cryptoalgo.codable.preferencesCoder;

import com.cryptoalgo.codable.Decoder;
import com.cryptoalgo.codable.DecodingException;
import com.cryptoalgo.codable.KeyedDecodingContainer;

import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class PreferencesDecoder<T extends Enum<T>> implements Decoder<T>, KeyedDecodingContainer<T> {
    private final Preferences prefsNode;

    PreferencesDecoder(String node) {
        prefsNode = Preferences.userRoot().node(node);
    }

    public Optional<Boolean> decodeBoolean(Enum forKey) throws DecodingException {
        return decodeBoolean(forKey, false);
    }
    public Optional<Boolean> decodeBoolean(Enum forKey, boolean def) throws DecodingException {
        String k = forKey.name();
        try {
            return prefsNode.nodeExists(k) ? Optional.of(prefsNode.getBoolean(k, def)) : Optional.empty();
        } catch (BackingStoreException e) {
            throw new DecodingException(k, "Boolean");
        }
    }

    public Optional<Integer> decodeInteger(Enum forKey) throws DecodingException {
        return decodeInteger(forKey, 0);
    }
    public Optional<Integer> decodeInteger(Enum forKey, int def) throws DecodingException {
        String k = forKey.name();
        try {
            return prefsNode.nodeExists(k) ? Optional.of(prefsNode.getInt(k, 0)) : Optional.empty();
        } catch (BackingStoreException e) {
            throw new DecodingException(k, "Integer");
        }
    }

    public Optional<String> decodeString(Enum forKey) {
        return null;
    }

    public KeyedDecodingContainer<T> container() {
        return null;
    }
}
