package com.tsg.hitbox;

public class Direction {
	private float i;

	public Direction(float i) {
		// TODO Auto-generated constructor stub
		this.i = (i%360);
	}
	
	public String toString() {
		return String.valueOf(this.i);
	}
	
	public float toFloat() {
		return this.i;
	}
	
	public void add(int num) {
		this.i += num;
		this.i = (i%360);
	}
	
	public void add(float num) {
		this.i += num;
		this.i = (i%360);
	}
	
	public void add(double num) {
		this.i += num;
		this.i = (i%360);
	}

	public void set(int num) {
		this.i = num;
		this.i = (i%360);
	}
	
	public void set(float num) {
		this.i = num;
		this.i = (i%360);
	}
	
	public void set(double num) {
		this.i = (float) num;
		this.i = (i%360);
	}
}
