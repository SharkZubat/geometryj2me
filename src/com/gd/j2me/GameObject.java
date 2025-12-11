package com.gd.j2me;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

import com.gd.j2me.SharkUtilities.Hitbox;

import java.io.IOException;
import java.util.*;

public class GameObject {
	
	private static Image obj001;
	
	public static void create(int i, float f, float g) {
		// TODO Auto-generated method stub
		
	}

	public static void render(int i, float f, float g, Graphics g2) throws IOException {
		// TODO Auto-generated method stub
		obj001 = Image.createImage("/img/obj/square_01_001.png");
		
		try {
			g2.drawImage(obj001, (int) f, (int) g, 0);
		} catch (Exception e) {
			
		}
		
		//g2.setColor(0xffffff);
		//g2.drawRect((int) f, (int) g, 20, 20);
	}

	public static void create(int i, float f, float g, List objid,
			float[] objx, float[] objy, int objlength, Hitbox[] objlengthhitbox) {
		// TODO Auto-generated method stub
		int objlenght = objlength;
        
		try {
			objx[objlenght] = f;
			objy[objlenght] = -g+20;
			
			objlengthhitbox[objlength] = Hitbox.rect(objx[objlength], objy[objlength], 20, 20);
		} catch (Exception e) {
			objlenght = 0;
			objx[objlenght] = f;
			objy[objlenght] = -g+20;

			objlengthhitbox[objlength] = Hitbox.rect(objx[objlength], objy[objlength], 20, 20);
		}
	}
	
	// todo: some objects system work
	// and i hope there's no any ducking errors at all
}
