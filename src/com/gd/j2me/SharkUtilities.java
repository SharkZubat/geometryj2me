package com.gd.j2me;

import javax.microedition.lcdui.Graphics;

public class SharkUtilities {

	public static class Hitbox {
		
		private float x = 0;
		private float y = 0;
		private float width = 0;
		private float height = 0;
		
		private Hitbox(float f, float g, float h, float i) {
			// TODO Auto-generated constructor stub
			this.x = f;
	        this.y = g;
	        this.width = h;
	        this.height = i;
		}

		public static boolean isTouching(Hitbox hitbox1, Hitbox hitbox2) {
			// TODO Auto-generated method stub
			if 		 ((hitbox2.getX() < hitbox1.getX() + hitbox1.getWidth()+1)
					&& (hitbox2.getX() + hitbox2.getWidth() > hitbox1.getX()-1)
					&& (hitbox2.getY() < hitbox1.getY() + hitbox1.getHeight()+1)
					&& (hitbox2.getY() + hitbox2.getHeight() > hitbox1.getY()-1)) {
				return true;
				//return hitbox2.getX() + ", " + (hitbox1.getX() + hitbox1.getWidth()+1) + ", " + (hitbox2.getX() <= hitbox1.getX() + hitbox1.getWidth()+1) + ", " + (hitbox2.getX() + hitbox2.getWidth() >= hitbox1.getX()-1) + ", " + (hitbox2.getY() <= hitbox1.getY() + hitbox1.getHeight()+1) + ", " + (hitbox2.getY() + hitbox2.getHeight() >= hitbox1.getY()-1);
			}
			return false;
		}

		public static Hitbox rect(float f, float g, float h, float i) {
			// TODO Auto-generated method stub
			return new Hitbox(f, g, h, i);
		}
		
		public float getX() {
			// TODO Auto-generated method stub
			SharkUtilities.Hitbox object = SharkUtilities.Hitbox();
			return x;
		}

		public float getY() {
			// TODO Auto-generated method stub
			return y;
		}

		public float getWidth() {
			// TODO Auto-generated method stub
			return width;
		}

		public float getHeight() {
			// TODO Auto-generated method stub
			return height;
		}

	}

	//public static Hitbox[] Hitbox;

	public static void drawHitbox(float i, float j, float k, float l, Graphics g,
			int m, int n) {
		// TODO Auto-generated method stub
		g.setColor(m);
		
		i = (int) i;
		j = (int) j;
		k = (int) k;
		l = (int) l;
		
		switch (n) {
			case (0): {
				g.drawLine((int) i, (int) j, (int) i+(int) k, (int) j);
				g.drawLine((int) i+(int) k, (int) j, (int) i+(int) k, (int) j+(int) l);
				g.drawLine((int) i+(int) k, (int) j+(int) l, (int) i, (int) j+(int) l);
				g.drawLine((int) i, (int) j+(int) l, (int) i, (int) j);
			}
		}
		
	}

	public static Hitbox Hitbox() {
		// TODO Auto-generated method stub
		return null;
	}

	public static void drawHitboxWithRect(Hitbox hitbox, Graphics g, int m, int n) {
		// TODO Auto-generated method stub
		g.setColor(m);
		
		float i = hitbox.getX();
		float j = hitbox.getY();
		float k = hitbox.getWidth();
		float l = hitbox.getHeight();
		
		switch (n) {
			case (0): {
				g.drawLine((int) i, (int) j, (int) i+(int) k, (int) j);
				g.drawLine((int) i+(int) k, (int) j, (int) i+(int) k, (int) j+(int) l);
				g.drawLine((int) i+(int) k, (int) j+(int) l, (int) i, (int) j+(int) l);
				g.drawLine((int) i, (int) j+(int) l, (int) i, (int) j);
			}
		}
		
	}
}
