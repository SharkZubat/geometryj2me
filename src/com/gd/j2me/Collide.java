package com.gd.j2me;

public class Collide {
	public static void collideground(PlayerScript player, double dt) {
		Vec2 psf = new Vec2(player.position.x, player.position.y);
		player.m_prevPos = new Vec2(player.position.x, player.position.y);
		int frames = (int) Math.floor((((float)dt)/(1f/240f))+1f);
		for (int i=0; i<frames; i++) {
			//System.out.println(i);
			psf.add(player.m_dXVel * 0.00416 * 54, (player.m_dYVel * 0.00416 * 54) + (player.m_dGravity * 0.00416 * 54)*i);
			if (psf.y < 15) {
				player.position.y = (float) (15 + ((frames-i)*0.0041666f + dt-(frames*0.0041666f)));
				player.m_dYVel = 1f/32;
				player.m_bIsRotating = false;
				player.m_bOnGround = true;
				player.m_obLastGroundPos = psf;
				break;
			}
		}
	}
}
