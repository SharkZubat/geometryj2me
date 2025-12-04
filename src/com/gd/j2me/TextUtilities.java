package com.gd.j2me;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;

public class TextUtilities {
	
	// The Shark's TextUtilities.
	
	public static void drawOutlinedString(String string, int i, int j, int k,
			Graphics g, int l, int m) {
		// TODO Auto-generated method stub
		g.setColor(m);
		g.drawString(string, i-1, j, k);
		g.setColor(m);
		g.drawString(string, i+1, j, k);
		g.setColor(m);
		g.drawString(string, i, j-1, k);
		g.setColor(m);
		g.drawString(string, i-1, j+1, k);
		
		g.setColor(l);
		g.drawString(string, i, j, k);
	}
	
	public static void setFont(int i, int j, int k, Graphics g) {
		g.setFont(Font.getFont(i, j, k));
	}
}
