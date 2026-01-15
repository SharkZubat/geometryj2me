package com.gd.j2me;

import com.tsg.hitbox.Direction;

class GameObjectType {
    private final int name;
    private final int ordinal;
    private static int upperBound = 0;

    private GameObjectType(int i) {
        this.name = i;
        this.ordinal = upperBound++;
    }
    
    public static final GameObjectType kGameObjectTypeSolid = new GameObjectType(0);
    public static final GameObjectType kGameObjectTypeHazard = new GameObjectType(2);
    public static final GameObjectType kGameObjectTypeInverseGravityPortal = new GameObjectType(3);
    public static final GameObjectType kGameObjectTypeNormalGravityPortal = new GameObjectType(4);
    public static final GameObjectType kGameObjectTypeShipPortal = new GameObjectType(5);
    public static final GameObjectType kGameObjectTypeCubePortal = new GameObjectType(6);
    public static final GameObjectType kGameObjectTypeDecoration = new GameObjectType(7);

    public int ordinal() {
        return this.ordinal;
    }

    public static int size() {
        return upperBound;
    }
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
