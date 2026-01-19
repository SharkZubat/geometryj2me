package com.gd.j2me;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Vector;
import com.tsg.hitbox.Direction;

/**
 * GMD File Parser for Geometry Dash levels
 * Parses .gmd XML files and extracts level objects
 * totally not ai
 */
public class GMDParser {
    
    // Object property keys
    private static final int KEY_OBJ_ID = 1;
    private static final int KEY_X_POS = 2;
    private static final int KEY_Y_POS = 3;
    private static final int KEY_FLIP_X = 4;
    private static final int KEY_FLIP_Y = 5;
    private static final int KEY_ROTATION = 6;
    
    /**
     * Parse a GMD file and return array of GameObjects
     */
    public static GameObject[] parseGMD(String filename) throws IOException {
        // Read the GMD file
        InputStream is = GMDParser.class.getResourceAsStream(filename);
        if (is == null) {
            System.out.println("GMD file not found: " + filename);
            return new GameObject[0];
        }
        
        // Read entire file into string
        StringBuffer sb = new StringBuffer();
        int ch;
        while ((ch = is.read()) != -1) {
            sb.append((char) ch);
        }
        is.close();
        
        String xmlContent = sb.toString();
        
        // Extract k2 data (object data)
        String objectData = extractTagContent(xmlContent, "<k>k2</k>");
        if (objectData == null || objectData.length() == 0) {
            System.out.println("No object data found in GMD");
            return new GameObject[0];
        }
        
        System.out.println("Found k2 data, length: " + objectData.length());
        
        // Try to decode and decompress
        String decodedData = decodeObjectData(objectData);
        if (decodedData == null) {
            System.out.println("Failed to decode object data");
            return new GameObject[0];
        }
        
        System.out.println("Decoded data length: " + decodedData.length());
        
        // Parse objects from decoded data
        return parseObjects(decodedData);
    }
    
    /**
     * Extract content between XML tags
     */
    private static String extractTagContent(String xml, String tag) {
        int startTagEnd = xml.indexOf(tag);
        if (startTagEnd == -1) return null;
        
        startTagEnd += tag.length();
        
        // Find the next <s> or <i> tag
        int contentStart = xml.indexOf("<s>", startTagEnd);
        if (contentStart == -1) {
            contentStart = xml.indexOf("<i>", startTagEnd);
        }
        if (contentStart == -1) return null;
        
        contentStart += 3; // Skip <s> or <i>
        
        // Find closing tag
        int contentEnd = xml.indexOf("</s>", contentStart);
        if (contentEnd == -1) {
            contentEnd = xml.indexOf("</i>", contentStart);
        }
        if (contentEnd == -1) return null;
        
        return xml.substring(contentStart, contentEnd);
    }
    
    /**
     * Decode the object data (Base64 + GZIP)
     */
    private static String decodeObjectData(String encoded) {
        try {
            // Decode Base64
            byte[] decoded = Base64Decoder.decode(encoded);
            System.out.println("Base64 decoded, got " + decoded.length + " bytes");
            
            // Check if data starts with GZIP magic number (0x1F 0x8B)
            if (decoded.length > 2 && 
                (decoded[0] & 0xFF) == 0x1F && 
                (decoded[1] & 0xFF) == 0x8B) {
                
                System.out.println("Data is GZIP compressed");
                
                // Try using our custom GZIP decoder
                byte[] decompressed = GZIPDecoder.decompress(decoded);
                if (decompressed != null && decompressed.length > 0) {
                    System.out.println("Decompressed successfully: " + decompressed.length + " bytes");
                    return new String(decompressed);
                }
                
                System.out.println("GZIP decompression failed, returning raw data");
            } else {
                System.out.println("Data is not GZIP compressed (or already decompressed)");
            }
            
            // Try to use the decoded data directly
            String result = new String(decoded);
            
            // Check if it looks like valid object data (should contain numbers and commas)
            if (result.indexOf(',') > 0 || result.indexOf(';') > 0) {
                System.out.println("Data appears to be valid object data");
                return result;
            }
            
            System.out.println("Data doesn't look like valid object data");
            System.out.println("First 100 chars: " + result.substring(0, Math.min(100, result.length())));
            
            return result;
            
        } catch (Exception e) {
            System.out.println("Decode error: " + e);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Parse objects from decoded object data string
     * Format: "1,objID,2,xPos,3,yPos,...;1,objID,2,xPos,3,yPos,...;"
     */
    private static GameObject[] parseObjects(String data) {
        Vector objects = new Vector();
        
        // Split by semicolon to get individual objects
        String[] objectStrings = split(data, ';');
        
        System.out.println("Found " + objectStrings.length + " object entries");
        
        for (int i = 0; i < objectStrings.length; i++) {
            String objStr = objectStrings[i].trim();
            if (objStr.length() == 0) continue;
            
            GameObject obj = parseObject(objStr);
            if (obj != null) {
                objects.addElement(obj);
            }
        }
        
        System.out.println("Parsed " + objects.size() + " objects successfully");
        
        // Convert Vector to array
        GameObject[] result = new GameObject[objects.size()];
        objects.copyInto(result);
        return result;
    }
    
    /**
     * Parse a single object from property string
     * Format: "1,objID,2,xPos,3,yPos,4,flipX,5,flipY,6,rotation,..."
     */
    private static GameObject parseObject(String objStr) {
        try {
            String[] parts = split(objStr, ',');
            
            // Default values
            int objID = 1;
            float xPos = 0;
            float yPos = 0;
            boolean flipX = false;
            boolean flipY = false;
            float rotation = 0;
            
            // Parse key-value pairs
            for (int i = 0; i < parts.length - 1; i += 2) {
                int key = parseInt(parts[i].trim());
                String value = parts[i + 1].trim();
                
                switch (key) {
                    case KEY_OBJ_ID:
                        objID = parseInt(value);
                        break;
                    case KEY_X_POS:
                        xPos = parseFloat(value);
                        break;
                    case KEY_Y_POS:
                        yPos = parseFloat(value);
                        break;
                    case KEY_FLIP_X:
                        flipX = parseInt(value) == 1;
                        break;
                    case KEY_FLIP_Y:
                        flipY = parseInt(value) == 1;
                        break;
                    case KEY_ROTATION:
                        rotation = parseFloat(value);
                        break;
                }
            }
            
            // Convert GD coordinates to game coordinates
            // GD uses 30 units per block, Y is flipped
            xPos = xPos;  // Keep X as is for now
            yPos = yPos;  // Keep Y as is for now
            
            Direction dir = new Direction((int) rotation);
            
            return new GameObject(objID, xPos, yPos, flipX, flipY, dir);
            
        } catch (Exception e) {
            System.out.println("Error parsing object: " + e);
            return null;
        }
    }
    
    /**
     * Split string by delimiter (J2ME doesn't have String.split)
     */
    private static String[] split(String str, char delimiter) {
        Vector parts = new Vector();
        StringBuffer current = new StringBuffer();
        
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == delimiter) {
                parts.addElement(current.toString());
                current = new StringBuffer();
            } else {
                current.append(c);
            }
        }
        
        // Add last part
        if (current.length() > 0) {
            parts.addElement(current.toString());
        }
        
        String[] result = new String[parts.size()];
        parts.copyInto(result);
        return result;
    }
    
    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private static float parseFloat(String s) {
        try {
            return Float.parseFloat(s);
        } catch (Exception e) {
            return 0f;
        }
    }
}