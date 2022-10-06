package com.cryptoalgo.oursql.support;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Manage storage of encrypted and unencrypted secrets
 */
public class SecretsStore {
    private static final Preferences prefsRoot = Preferences.userNodeForPackage(SecretsStore.class);
    private static final String algo = "AES/GCM/NoPadding";

    /**
     * An exception for various failures during storage/retrieval of secrets
     */
    public static class StoreException extends Exception {
        /**
         * Create an instance of the exception with a reason
         * @param reason Reason of exception
         */
        public StoreException(String reason) { super(reason); }
    }

    private static byte[] getRandomBytes(int len) {
        byte[] r = new byte[len];
        new SecureRandom().nextBytes(r);
        return r;
    }
    private static GCMParameterSpec generateIV() {
        return new GCMParameterSpec(128, getRandomBytes(16));
    }

    private static SecretKey deriveKey(String password, byte[] salt)
        throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(
            password.toCharArray(),
            salt,
            65536,
            256
        );
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    private static byte[] crypt(
        String algorithm,
        byte[] input,
        SecretKey key,
        GCMParameterSpec iv,
        int op
    ) throws NoSuchPaddingException, NoSuchAlgorithmException,
        InvalidAlgorithmParameterException, InvalidKeyException,
        BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(op, key, iv);
        return cipher.doFinal(input);
    }

    // Public encryption/decryption methods

    /**
     * Store text in plaintext form without any encryption
     * @param plainText Plain text to store
     * @param storeKey A key to use for storage in Java Preferences
     */
    public static void encrypt(String plainText, String storeKey) {
        Preferences storeNode = prefsRoot.node(storeKey);
        storeNode.put("algo", "plain");
        storeNode.putByteArray("cipher", plainText.getBytes());
    }

    /**
     * Store text encrypted in AES-GCM. Calls {@link #encrypt(String, String, String, String)}
     * with the default algorithm, currently <code>AES/GCM/NoPadding</code>
     * @param plainText Plain text to store
     * @param password Encryption password
     * @param storeKey A key to use for storage in Java Preferences
     * @throws StoreException If encryption failed
     * @see #encrypt(String, String, String, String)
     */
    public static void encrypt(String plainText, String password, String storeKey) throws StoreException {
        encrypt(plainText, password, algo, storeKey);
    }

    /**
     * Store text encrypted in a user-specified algo.
     * @param plainText Plain text to store
     * @param password Encryption password
     * @param algo Encryption algorithm
     * @param storeKey A key to use for storage in Java Preferences
     * @throws StoreException If encryption failed
     */
    public static void encrypt(
        String plainText,
        String password,
        String algo,
        String storeKey
    ) throws StoreException {
        byte[] salt = getRandomBytes(32), cipher;
        GCMParameterSpec iv = generateIV();
        try {
            cipher = crypt(algo, plainText.getBytes(), deriveKey(password, salt), iv, Cipher.ENCRYPT_MODE);
        } catch (Exception e) {
            e.printStackTrace();
            throw new StoreException("Failed to encrypt plainText");
        }
        Preferences storeNode = prefsRoot.node(storeKey);
        storeNode.putByteArray("salt", salt);
        storeNode.putByteArray("iv", iv.getIV());
        storeNode.putByteArray("cipher", cipher);
        storeNode.put("algo", algo);
    }

    /**
     * Retrieve encrypted data in plaintext form
     * @param password Encryption password
     * @param storeKey Preferences key to retrieve secret from
     * @throws StoreException If decryption failed for whatever reason (e.g. wrong password)
     * @return Decrypted secret
     */
    public static String decrypt(String password, String storeKey) throws StoreException {
        // Firstly ensure data is encrypted
        if (!isEncrypted(storeKey)) throw new StoreException(
            "Data is stored in plaintext form, please use decrypt(String storeKey) for retrieval instead"
        );

        Preferences storeNode = prefsRoot.node(storeKey);
        // Retrieve info from preferences
        byte[]
            salt = storeNode.getByteArray("salt", null),
            cipher = storeNode.getByteArray("cipher", null);
        GCMParameterSpec iv = new GCMParameterSpec(128, storeNode.getByteArray("iv", null));
        try {
            return new String(crypt(algo, cipher, deriveKey(password, salt), iv, Cipher.DECRYPT_MODE));
        } catch (Exception e) {
            throw new StoreException("Failed to decrypt plainText! Ensure password is valid");
        }
    }

    /**
     * Retrieves data stored in plaintext form
     * @param storeKey Preferences key to retrieve secret from
     * @return Retrieved secret
     * @throws StoreException If decryption failed for whatever reason (e.g. the secret is encrypted)
     */
    public static String decrypt(String storeKey) throws StoreException {
        if (isEncrypted(storeKey)) throw new StoreException(
            "Data is stored in encrypted form, please use decrypt(String password, String storeKey) instead"
        );
        return new String(prefsRoot.node(storeKey).getByteArray("cipher", null));
    }

    /**
     * Check if stored data at a particular key is stored in encrypted form.
     * Does extensive sanity checking to ensure all required keys are present.
     * @param storeKey Key of secret to check encryption status of
     * @return True if data is stored in encrypted form, false if it's stored in plaintext
     * @throws StoreException If the secret could not be retrieved or expected values were missing
     */
    public static boolean isEncrypted(String storeKey) throws StoreException {
        try {
            if (!prefsRoot.nodeExists(storeKey))
                throw new StoreException("No secrets exists at requested path " + storeKey);
        } catch (BackingStoreException e) {
            throw new StoreException("Exception when checking for storeKey existence");
        }
        Preferences n = prefsRoot.node(storeKey);

        // Check if algo exists
        String algo = n.get("algo", null);
        if (algo == null) throw new StoreException("Algo param missing!");
        // Ensure cipher exists
        if (n.getByteArray("cipher", null) == null)
            throw new StoreException("Stored cipher missing!");

        // Validate specific keys to ensure they can be retrieved
        if (algo.equals("plain")) return false;
        else { // Possibly encrypted
            // Do sanity checking to ensure all expected values are present.
            // If anything's missing, throw an exception
            if (n.getByteArray("salt", null) == null
                || n.getByteArray("iv", null) == null
            ) throw new StoreException("Required cipher parameters are missing!");
            return true;
        }
    }

    /**
     * Convenience method to check if a particular secret is encrypted.
     * Catches exceptions and returns a default value instead.
     * @param storeKey Key of secret to check encryption status of
     * @param def Value to return if an exception occurred while checking if the secret is encrypted
     * @return True if the secret at <code>storeKey</code> is encrypted,
     * @see #isEncrypted(String storeKey)
     */
    public static boolean isEncrypted(String storeKey, boolean def) {
        try { return isEncrypted(storeKey); }
        catch (StoreException e) { return def; }
    }

    /**
     * Deletes a secret stored at a specified storeKey
     * @param storeKey Key of secret to delete
     * @throws BackingStoreException If the underlying Preferences storage driver threw an exception
     */
    public static void remove(String storeKey) throws BackingStoreException {
        try { prefsRoot.node(storeKey).removeNode(); }
        catch (IllegalStateException ignored) {}
    }

    /**
     * Check if a secret  is present at the specified storeKey. Only checks for the
     * existence, not the validity, of a secret.
     * @param storeKey key to check
     * @return True if data is present at the specified storeKey
     */
    public static boolean exists(String storeKey) {
        try { return prefsRoot.nodeExists(storeKey); }
        catch (BackingStoreException e) { return false; }
    }
}
