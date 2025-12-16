package com.gd.j2me;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

import com.gd.j2me.SharkUtilities.Hitbox;

import java.io.IOException;
import java.util.*;

public class GameObject {
	
	private static Image[] objimage = new Image[100];
	private static int[] hitboxids = new int[100];
	
	private static void setupimgs() throws IOException {
		objimage[1] = Image.createImage("/img/obj/square_01_001.png");
		objimage[8] = Image.createImage("/img/obj/spike_01_001.png");
	}
	
	private static void setuphtypes() {
		hitboxids[1] = 0;
		hitboxids[2] = 0;
		hitboxids[3] = 0;
		hitboxids[4] = 0;
		hitboxids[5] = 0;
		hitboxids[6] = 0;
		hitboxids[7] = 0;
		hitboxids[8] = 1;
		hitboxids[9] = 1;
	}
	
	public static int getHitboxType(int i) {
		//some values will return:
		//0 - solid
		//1 - hazard
		//2 - portal/pad
		//3 - orb/ring
		//4 - untouched
		
		setuphtypes();
		
		return hitboxids[i];
	}
	
	public static Image getImage(int i) throws IOException {
		setupimgs();
		
		return objimage[i];
	}

	public static void render(int i, float f, float g, double h, double i2, Image image2, Graphics g2) throws IOException {
		// TODO Auto-generated method stub
		Image image = image2;
		
		try {
			SharkUtilities.drawImageWithAnchor(image, (int) f, (int) g, 0, h, i2, g2);
		} catch (Exception e) {
			
		}
		
		//g2.setColor(0x80ffffff);
		//g2.drawRect((int) f, (int) g, 20, 20);
	}
	
	private static void setuphitboxes(int i, int objlength, float[] objx, float[] objy, Hitbox[] objlengthhitbox) {
		switch (i) {
			case (8): {
				objlengthhitbox[objlength] = Hitbox.rect(objx[objlength]+16, objy[objlength]+12, 4, 8, 0.5, 0.5);
				break;
			}
			default: {
				objlengthhitbox[objlength] = Hitbox.rect(objx[objlength], objy[objlength], 20, 20, 0.5, 0.5);
				break;
			}
		}
	}

	public static void create(int i, int f, int g, int[] objid,
			float[] objx, float[] objy, int objlength, Hitbox[] objlengthhitbox) {
		// TODO Auto-generated method stub
		int objlenght = objlength;
        
		try {
			objx[objlenght] = f;
			objy[objlenght] = -g;
			objid[objlength] = i;
			
			setuphitboxes(i, objlength, objx, objy, objlengthhitbox);
		} catch (Exception e) {
			objlenght = 0;
			objx[objlenght] = f;
			objy[objlenght] = -g;
			objid[objlength] = i;
			
			setuphitboxes(i, objlength, objx, objy, objlengthhitbox);
		}
	}
	
	// todo: some objects system work
	// and i hope there's no any ducking errors at all
}
