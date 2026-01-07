package com.gd.j2me;

class PlayerGamemode {
    private final int name;
    private final int ordinal;
    private static int upperBound = 0;

    private PlayerGamemode(int i) {
        this.name = i;
        this.ordinal = upperBound++;
    }
    
    public static final PlayerGamemode PlayerGamemodeCube = new PlayerGamemode(0);
    public static final PlayerGamemode PlayerGamemodeShip = new PlayerGamemode(1);

    public int ordinal() {
        return this.ordinal;
    }

    public static int size() {
        return upperBound;
    }
}

public class PlayerScript {
	private double m_dYVel = 0;
	private double m_dGravity = 0.958199;
	private double m_dJumpHeight = 11.180032;
	private PlayerGamemode gamemode = PlayerGamemode.PlayerGamemodeCube;
	
	private boolean m_bOnGround;

	private boolean m_bIsDead;
	private boolean m_bIsLocked;

	private boolean m_bGravityFlipped;

	private boolean m_isRising;
	
	public PlayerScript() {
		//
	}
}
