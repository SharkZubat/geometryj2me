package com.gd.j2me;

import java.util.Hashtable;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

public class CustomFont {
	private static final String TARGET = "~`!1@2#3$4%5^6&7*8(9)0_-+=[]{}:;\"'|\\<>,.?/QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm ";

	private static Hashtable scaledCache = new Hashtable();

    private static int getChar(char onechar) {
        for (int i = 0; i < TARGET.length(); i++) {
            if (TARGET.charAt(i) == onechar) return i;
        }
        return 512;
    }

    public static void drawString(Image image, int x, int y, float scale, String string, int widthSpace, Graphics g) {
        int spaceWidthScale = (int) (widthSpace * scale);
        int baseWidth = image.getWidth() / TARGET.length();
        int baseHeight = image.getHeight();

        for (int i = 0; i < string.length(); i++) {
            char currentChar = string.charAt(i);
            String key = currentChar + "_" + scale;
            
            Image charImg = (Image) scaledCache.get(key);

            if (charImg == null) {
                int charIndex = getChar(currentChar);
                Image tempClip;
                
                if (charIndex != 512) {
                    tempClip = SharkUtilities.splitImg(image, charIndex * baseWidth, 0, baseWidth, baseHeight);
                } else {
                    tempClip = SharkUtilities.splitImg(image, 0, 0, 1, 1);
                }
                
                charImg = SharkUtilities.scale(tempClip, (int)(tempClip.getWidth() * scale), (int)(tempClip.getHeight() * scale));
                scaledCache.put(key, charImg);
            }

            g.drawImage(charImg, x + (i * spaceWidthScale), y, Graphics.TOP | Graphics.LEFT);
        }
    }
    
    public static void clearCache() {
        scaledCache.clear();
    }
}
