package com.gd.j2me;

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

}
