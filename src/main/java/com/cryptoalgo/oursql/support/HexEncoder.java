package com.cryptoalgo.oursql.support;

import java.nio.charset.StandardCharsets;

/**
 * Utility class to encode byte arrays into hex strings
 */
public class HexEncoder {
    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    /**
     * Encode a byte array into a hex string
     * @param bytes Byte array to encode
     * @return Hex representation of byte array
     */
    public static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }
}
