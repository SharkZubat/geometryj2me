package com.gd.j2me;

import javax.microedition.lcdui.Graphics;

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
	
	float m_playerSpeed = 0.9f;
	float m_snapDifference;
	
	boolean _particles1Activated;
	boolean _particles2Activated;
	boolean _particles3Activated;
	
	Vec2 m_obLastGroundPos;
	Vec2 m_prevPos;

	double m_dXVel = 5.770002;
	
	public float x;
	public float y=15;
	public Direction dir = new Direction(0);
	
	public PlayerScript() {
		//
	}
	
	public void render(Graphics g, float cx, float cy) {
		g.setColor(0xffffff);
		g.fillRect((int)(0-cx/1.363636), (int)(-0+cy/1.363636), 22, 22);
	}
	
	public void update(double delta) {
		//dir.add(-360*delta);
		x += m_dXVel * delta * 54;
	}
}
