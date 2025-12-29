package com.gd.j2me;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

public class CustomFont {
	private static int getChar(char onechar) {
		String target = "~`!1@2#3$4%5^6&7*8(9)0_-+=[]{}:;\"'|\\<>,.?/QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm";
		
		for (int i = 0; i < target.length(); i++) {
			if (target.charAt(i) == onechar) {
				return i;
			}
		}
		
		return 512;
	}
	
	public static void drawString(Image image, int x, int y, float scale, String string, int widthSpace, Graphics g) {
		widthSpace *= scale;
		String target = "~`!1@2#3$4%5^6&7*8(9)0_-+=[]{}:;\"'|\\<>,.?/QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm";
		for (int i = 0; i < string.length(); i++) {
			//Image splitImage = Image.createImage(image.getWidth(), image.getHeight());
			Image tempImage = Image.createImage(1000, 10);
			if (getChar(string.charAt(i)) != 512) {
				tempImage = SharkUtilities.splitImg(image, (image.getWidth() / target.length())*getChar(string.charAt(i)), 0, image.getWidth() / target.length(), image.getHeight());
			} else {
				tempImage = SharkUtilities.scale(SharkUtilities.splitImg(image, 0, 0, 1, 1), 10, 10);
			}
			Image tempImage2 = SharkUtilities.scale(tempImage, (int)(tempImage.getWidth() * scale), (int)(tempImage.getHeight() * scale));
			//System.out.println(tempImage.getWidth() * scale);
			//Graphics g2 = splitImage.getGraphics();
			//g2.drawRegion(image, (image.getWidth() / target.length())*getChar(target.charAt(i)), 0, image.getWidth() / target.length(), image.getHeight(), Sprite.TRANS_NONE, 0, 0, Graphics.TOP | Graphics.LEFT);
			g.drawImage(tempImage2, (int) (x + ((widthSpace * i))), y, 0);
		}
	}
}
