package com.gd.j2me;

import com.tsg.hitbox.Direction;

class GameObjectType {
    public static final int kGameObjectTypeSolid = 0;
    public static final int kGameObjectTypeHazard = 2;
    public static final int kGameObjectTypeInverseGravityPortal = 3;
    public static final int kGameObjectTypeNormalGravityPortal = 4;
    public static final int kGameObjectTypeShipPortal = 5;
    public static final int kGameObjectTypeCubePortal = 6;
    public static final int kGameObjectTypeDecoration = 7;
}
public class GameObject {
	public int id;
	public float x;
	public float y;
	public boolean h;
	public boolean v;
	public Direction dir = new Direction(0);
	public GameObject(int id, float x, float y, boolean h, boolean v, Direction dir) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.h = h;
		this.v = v;
		this.dir = dir;
	}
	
	public static String[] getImages() {
		String[] output = {
			"square_01_001.png",
			"square_02_001.png",
			"square_03_001.png",
			"square_04_001.png",
			"square_05_001.png",
			"square_06_001.png",
			"square_07_001.png",
			"spike_01_001.png"
		};
		return output;
	}
}
