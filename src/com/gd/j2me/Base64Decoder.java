package com.gd.j2me;

/**
 * Decodes Base64 encoded strings to byte arrays
 */
public class Base64Decoder {
    private static final String BASE64_CHARS = 
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    
    public static byte[] decode(String input) {
        if (input == null || input.length() == 0) {
            return new byte[0];
        }
        
        // Remove any whitespace and padding
        input = input.trim();
        int padding = 0;
        if (input.endsWith("==")) {
            padding = 2;
        } else if (input.endsWith("=")) {
            padding = 1;
        }
        
        int length = input.length();
        int outputLength = (length * 3) / 4 - padding;
        byte[] output = new byte[outputLength];
        
        int outputIndex = 0;
        int buffer = 0;
        int bitsCollected = 0;
        
        for (int i = 0; i < length; i++) {
            char c = input.charAt(i);
            if (c == '=') {
                break;
            }
            
            int value = BASE64_CHARS.indexOf(c);
            if (value == -1) {
                continue; // Skip invalid characters
            }
            
            buffer = (buffer << 6) | value;
            bitsCollected += 6;
            
            if (bitsCollected >= 8) {
                bitsCollected -= 8;
                if (outputIndex < output.length) {
                    output[outputIndex++] = (byte) ((buffer >> bitsCollected) & 0xFF);
                }
            }
        }
        
        return output;
    }
}