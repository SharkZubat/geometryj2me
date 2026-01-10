package com.gd.j2me;

public class Direction {
	private float i;

	public Direction(float i) {
		// TODO Auto-generated constructor stub
		this.i = (((i+180)%360)-180);
	}
	
	public String toString() {
		return String.valueOf(this.i);
	}
	
	public float toFloat() {
		return this.i;
	}
	
	public void add(int num) {
		this.i += num;
	}
	
	public void add(float num) {
		this.i += num;
	}
	
	public void add(double num) {
		this.i += num;
	}
}
