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
	public static double m_dYVel = 0;
	double m_dGravity = 0.958199;
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
	Vec2 m_prevPos = new Vec2(0, 15);

	double m_dXVel = 5.770002;
	
	public Vec2 position = new Vec2(0, 15);
	public Direction dir = new Direction(0);
	public boolean m_movingState;
	
	public PlayerScript() {
		maxvely();
		//
	}
	
	public void render(Graphics g, float cx, float cy) {
		g.setColor(0xffffff);
		g.fillRect((int)(0-cx/1.363636), (int)(-0+cy/1.363636), 22, 22);
	}
	
	public void update(double delta) {
		//dir.add(-360*delta);
		m_dYVel -= m_dGravity * delta * 54;
		position.add(m_dXVel * delta * 54, m_dYVel * delta * 54);
		Collide.collideground(this, delta);
		maxvely();
	    Launcher.levelGame.cameraX=Launcher.levelGame.curr_player.position.x+(Launcher.levelGame.getWidth()/6);
	}
	
	private void maxvely() {
		switch (gamemode.ordinal()) {
			case 0: {
				m_dYVel = Math.min(20, Math.max(-15, m_dYVel));
				break;
			}
			case 1: {
				m_dYVel = Math.min(7, Math.max(-6.4, m_dYVel));
				break;
			}
		}
	}
}
