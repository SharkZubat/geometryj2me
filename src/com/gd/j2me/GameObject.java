package com.gd.j2me;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

import com.gd.j2me.SharkUtilities.Hitbox;

import java.io.IOException;
import java.util.*;

public class GameObject {
	public static class Object {
	    public int objid;
	    public float objx;
	    public float objy;
	    public boolean isFlippedHor;
	    public boolean isFlippedVer;
	    public float rotation;
	    
	    public Object(int num1, float num2, float num3, boolean num4, boolean num5, float num6) {
	    	this.objid = num1;
	    	this.objx = num2;
	    	this.objy = num3;
	    	this.isFlippedHor = num4;
	    	this.isFlippedVer = num5;
	    	this.rotation = num6;
	    }
	}
	
	private static Image[] objimage = new Image[100];
	private static int[] hitboxids = new int[100];
	// for portals, pads, rings only
	private boolean isTouched = false;
	
	private static void setupimgs() throws IOException {
		objimage[1] = Image.createImage("/img/obj/square_01_001.png");
		objimage[2] = Image.createImage("/img/obj/square_02_001.png");
		objimage[3] = Image.createImage("/img/obj/square_03_001.png");
		objimage[4] = Image.createImage("/img/obj/square_04_001.png");
		objimage[5] = Image.createImage("/img/obj/square_05_001.png");
		objimage[6] = Image.createImage("/img/obj/square_06_001.png");
		objimage[7] = Image.createImage("/img/obj/square_07_001.png");
		objimage[8] = Image.createImage("/img/obj/spike_01_001.png");
		objimage[9] = Image.createImage("/img/obj/pit_01_001.png");
		objimage[10] = Image.createImage("/img/obj/portal_01_001.png");
		objimage[11] = Image.createImage("/img/obj/portal_02_001.png");
		objimage[12] = Image.createImage("/img/obj/portal_03_001.png");
		objimage[13] = Image.createImage("/img/obj/portal_04_001.png");
		objimage[14] = Image.createImage("/img/obj/square_05_001.png");
	}
	
	private static void setuphtypes() {
		hitboxids[1] = 0;
		hitboxids[2] = 0;
		hitboxids[3] = 0;
		hitboxids[4] = 0;
		hitboxids[5] = 4;
		hitboxids[6] = 0;
		hitboxids[7] = 0;
		hitboxids[8] = 1;
		hitboxids[9] = 1;
		hitboxids[10] = 2;
		hitboxids[11] = 2;
		hitboxids[12] = 2;
		hitboxids[13] = 2;
		hitboxids[14] = 4;
	}
	
	public static int getHitboxType(int i) {
		//some values will return:
		//0 - solid
		//1 - hazard
		//2 - portal/pad
		//3 - orb/ring
		//4 - untouched/decoration
		
		setuphtypes();
		
		return hitboxids[i];
	}
	
	public static Image getImage(int i) throws IOException {
		setupimgs();
		
		return objimage[i];
	}

	public static void render(int i, float f, float g, double h, double i2, Image[] image2, Graphics g2) throws IOException {
		// TODO Auto-generated method stub
		Image[] image = image2;
		
		switch (i) {
			case 10: {
				if (i == 10)
				SharkUtilities.drawImageWithAnchor(image[i], (int) f+10, (int) g, 0, h, i2, g2);
			}
			case 11: {
				if (i == 11)
				SharkUtilities.drawImageWithAnchor(image[i], (int) f+10, (int) g, 0, h, i2, g2);
			}
			case 12: {
				if (i == 12)
				SharkUtilities.drawImageWithAnchor(image[i], (int) f+10, (int) g, 0, h, i2, g2);
			}
			case 13: {
				if (i == 13)
				SharkUtilities.drawImageWithAnchor(image[i], (int) f+10, (int) g, 0, h, i2, g2);
			}
			case 14: {
				if (i == 14)
				SharkUtilities.drawImageWithAnchor(image[i], (int) f, (int) g, 0, h, i2, g2);
			}
			default: {
				if (i != 10 &&
						i != 11 &&
						i != 12 &&
						i != 13 &&
						i != 14)
				SharkUtilities.drawImageWithAnchor(image[i], (int) f, (int) g, 0, h, i2, g2);
			}
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
			case (9): {
				objlengthhitbox[objlength] = Hitbox.rect(objx[objlength]+14, objy[objlength]+12.8f, 6, 7.2f, 0.5, 0.5);
				break;
			}
			case (10): {
				objlengthhitbox[objlength] = Hitbox.rect(objx[objlength]+3.3f+20, objy[objlength]-30f, 16.7f, 50, 0.5, 0.5);
				break;
			}
			case (11): {
				objlengthhitbox[objlength] = Hitbox.rect(objx[objlength]+3.3f+20, objy[objlength]-30f, 16.7f, 50, 0.5, 0.5);
				break;
			}
			case (12): {
				objlengthhitbox[objlength] = Hitbox.rect(objx[objlength]+3.3f+20, objy[objlength]-30f, 16.7f, 50, 0.5, 0.5);
				break;
			}
			case (13): {
				objlengthhitbox[objlength] = Hitbox.rect(objx[objlength]+3.3f+20, objy[objlength]-30f, 16.7f, 50, 0.5, 0.5);
				break;
			}
			default: {
				objlengthhitbox[objlength] = Hitbox.rect(objx[objlength], objy[objlength], 20, 20, 0.5, 0.5);
				break;
			}
		}
	}

	public static void create(int i, int f, float f2, int[] objid,
			float[] objx, float[] objy, int objlength, Hitbox[] objlengthhitbox) {
		// TODO Auto-generated method stub
		int objlenght = objlength;
        
		try {
			objx[objlenght] = f;
			objy[objlenght] = -f2;
			objid[objlength] = i;
			
			setuphitboxes(i, objlength, objx, objy, objlengthhitbox);
		} catch (Exception e) {
			objlenght = 0;
			objx[objlenght] = f;
			objy[objlenght] = -f2;
			objid[objlength] = i;
			
			setuphitboxes(i, objlength, objx, objy, objlengthhitbox);
		}
	}
	
	public static Image appendImage(int id, Image[] image) {
		switch (id) {
			default: {
				return image[id];
			}
		};
	}
	
	public void setToTouched(int id, boolean[] objdataistouched) {
		objdataistouched[id] = true;
	}
	
	// todo: some objects system work
	// and i hope there's no any ducking errors at all
}
