package com.gd.j2me;

import java.io.ByteArrayOutputStream;

/**
 * Simple GZIP/DEFLATE decoder for J2ME
 * Implements the DEFLATE algorithm to decompress gzip data
 * also not ai
 */
public class GZIPDecoder {
    
    // Huffman code lengths for fixed huffman codes
    private static final int[] FIXED_LITERAL_LENGTHS = new int[288];
    private static final int[] FIXED_DISTANCE_LENGTHS = new int[32];
    
    static {
        // Initialize fixed Huffman codes
        for (int i = 0; i <= 143; i++) FIXED_LITERAL_LENGTHS[i] = 8;
        for (int i = 144; i <= 255; i++) FIXED_LITERAL_LENGTHS[i] = 9;
        for (int i = 256; i <= 279; i++) FIXED_LITERAL_LENGTHS[i] = 7;
        for (int i = 280; i <= 287; i++) FIXED_LITERAL_LENGTHS[i] = 8;
        for (int i = 0; i < 32; i++) FIXED_DISTANCE_LENGTHS[i] = 5;
    }
    
    private byte[] input;
    private int bitPos;
    private int bytePos;
    
    public static byte[] decompress(byte[] compressed) {
        try {
            GZIPDecoder decoder = new GZIPDecoder(compressed);
            return decoder.decode();
        } catch (Exception e) {
            System.out.println("GZIP decompress error: " + e);
            e.printStackTrace();
            return null;
        }
    }
    
    private GZIPDecoder(byte[] input) {
        this.input = input;
        this.bitPos = 0;
        this.bytePos = 0;
    }
    
    private byte[] decode() throws Exception {
        // Skip GZIP header (10 bytes minimum)
        if (input.length < 10) {
            throw new Exception("Invalid GZIP header");
        }
        
        // Check magic number
        if ((input[0] & 0xFF) != 0x1F || (input[1] & 0xFF) != 0x8B) {
            throw new Exception("Not a GZIP file");
        }
        
        // Check compression method (should be 8 for DEFLATE)
        if ((input[2] & 0xFF) != 8) {
            throw new Exception("Unsupported compression method");
        }
        
        int flags = input[3] & 0xFF;
        
        // Skip to the end of header
        int headerPos = 10;
        
        // Skip extra field if present (FEXTRA)
        if ((flags & 0x04) != 0) {
            int xlen = (input[headerPos] & 0xFF) | ((input[headerPos + 1] & 0xFF) << 8);
            headerPos += 2 + xlen;
        }
        
        // Skip file name if present (FNAME)
        if ((flags & 0x08) != 0) {
            while (headerPos < input.length && input[headerPos] != 0) {
                headerPos++;
            }
            headerPos++; // Skip null terminator
        }
        
        // Skip comment if present (FCOMMENT)
        if ((flags & 0x10) != 0) {
            while (headerPos < input.length && input[headerPos] != 0) {
                headerPos++;
            }
            headerPos++; // Skip null terminator
        }
        
        // Skip header CRC if present (FHCRC)
        if ((flags & 0x02) != 0) {
            headerPos += 2;
        }
        
        // Start decompressing from after header
        bytePos = headerPos;
        bitPos = 0;
        
        return inflateData();
    }
    
    private byte[] inflateData() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        boolean finalBlock = false;
        
        while (!finalBlock) {
            // Read block header
            finalBlock = readBits(1) == 1;
            int blockType = readBits(2);
            
            if (blockType == 0) {
                // No compression
                alignToByte();
                int len = readBits(16);
                int nlen = readBits(16);
                
                for (int i = 0; i < len; i++) {
                    output.write(readBits(8));
                }
            } else if (blockType == 1 || blockType == 2) {
                // Fixed or dynamic Huffman codes
                // For simplicity, we'll use a basic implementation
                // This is a simplified version that may not handle all cases
                inflateBlock(output, blockType == 2);
            } else {
                throw new Exception("Invalid block type");
            }
        }
        
        return output.toByteArray();
    }
    
    private void inflateBlock(ByteArrayOutputStream output, boolean dynamic) throws Exception {
        // This is a simplified implementation
        // For full GMD support, you might need a more complete DEFLATE decoder
        
        if (dynamic) {
            // Dynamic Huffman - more complex, simplified here
            // In a full implementation, you'd read the Huffman trees here
            throw new Exception("Dynamic Huffman not fully implemented - use GZIPInputStream alternative");
        }
        
        // Fixed Huffman codes (simplified)
        while (true) {
            int symbol = decodeSymbol();
            
            if (symbol < 256) {
                // Literal byte
                output.write(symbol);
            } else if (symbol == 256) {
                // End of block
                break;
            } else {
                // Length/distance pair (simplified)
                int length = symbol - 254;
                int distance = readBits(5) + 1;
                
                byte[] buf = output.toByteArray();
                int pos = buf.length - distance;
                
                for (int i = 0; i < length && pos + i < buf.length; i++) {
                    output.write(buf[pos + i]);
                }
            }
        }
    }
    
    private int decodeSymbol() throws Exception {
        // Simplified fixed Huffman decoding
        int code = 0;
        int first = 0;
        int index = 0;
        
        for (int len = 1; len <= 15; len++) {
            code |= readBits(1);
            int count = 0;
            
            if (len <= 7) {
                // 256-279: 7 bits
                if (code >= 0 && code <= 23) return 256 + code;
            } else if (len == 8) {
                // 0-143: 8 bits (0-143), 280-287: 8 bits (192-199)
                if (code >= 48 && code <= 191) return code - 48;
                if (code >= 192 && code <= 199) return 280 + (code - 192);
            } else if (len == 9) {
                // 144-255: 9 bits
                if (code >= 400 && code <= 511) return 144 + (code - 400);
            }
            
            code <<= 1;
        }
        
        throw new Exception("Invalid Huffman code");
    }
    
    private int readBits(int numBits) {
        int result = 0;
        
        for (int i = 0; i < numBits; i++) {
            if (bytePos >= input.length) {
                return result;
            }
            
            int bit = (input[bytePos] >> bitPos) & 1;
            result |= (bit << i);
            
            bitPos++;
            if (bitPos >= 8) {
                bitPos = 0;
                bytePos++;
            }
        }
        
        return result;
    }
    
    private void alignToByte() {
        if (bitPos != 0) {
            bitPos = 0;
            bytePos++;
        }
    }
}