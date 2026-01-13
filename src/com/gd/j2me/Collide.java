package com.gd.j2me;

public class Collide {
	public static void collideground(PlayerScript player, double dt) {
		PlayerScript psf = player;
		psf.m_movingState = true;
		int frames = (int) Math.floor((((float)dt/1000f)/(1f/240f))+1f);
		for (int i=0; i<frames; i++) {
			psf.update(0.00416f);
			Launcher.levelGame.curr_player.position.y = 15;
			if (psf.position.y < 15) {
				Launcher.levelGame.curr_player.position.y = 15;
				Launcher.levelGame.curr_player.m_dYVel = 0.0032;
				break;
			}
		}
	}
}
