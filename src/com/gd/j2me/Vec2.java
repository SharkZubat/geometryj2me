package com.gd.j2me;

public class Vec2 {
	public float x;
	public float y;

	public Vec2(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public void add(float x, float y) {
		this.x += x;
		this.y += y;
	}
	
	public void add(double x, double y) {
		this.x += x;
		this.y += y;
	}
	
	public void add(int x, int y) {
		this.x += x;
		this.y += y;
	}
}
