package com.gd.j2me;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.gd.j2me.SharkUtilities.Hitbox;

public class PlayerObject {
	public static void renderPlayer(Image player_2, Image player, Image ship, float playerAngle, int playerXint, float cameraX, int playerYint, float cameraY, int type, Graphics g) {
		switch (type) {
			case (1): {
				SharkUtilities.drawImageWithAnchorOld(SharkUtilities.rotateImage(SharkUtilities.scale(player_2, 10, 10), playerAngle), playerXint - (int)(Math.sin(playerAngle / Math.PI / 17.5)*4)- (int)cameraX - (int)((SharkUtilities.rotateImageToReturnWidth(SharkUtilities.scale(player_2, 10, 10), playerAngle) - 20) / 1), playerYint - (int)(Math.cos(playerAngle / Math.PI / 17.5)*4) - (int)cameraY - (int)((SharkUtilities.rotateImageToReturnHeight(SharkUtilities.scale(player_2, 10, 10), playerAngle) - 20) / 1), 0, 0.5, 0.5, g);
				SharkUtilities.drawImageWithAnchorOld(SharkUtilities.rotateImage(SharkUtilities.scale(player, 10, 10), playerAngle), playerXint - (int)(Math.sin(playerAngle / Math.PI / 17.5)*4) - (int)cameraX - (int)((SharkUtilities.rotateImageToReturnWidth(SharkUtilities.scale(player, 10, 10), playerAngle) - 20) / 1), playerYint - (int)(Math.cos(playerAngle / Math.PI / 17.5)*4) - (int)cameraY - (int)((SharkUtilities.rotateImageToReturnHeight(SharkUtilities.scale(player, 10, 10), playerAngle) - 20) / 1), 0, 0.5, 0.5, g);
				SharkUtilities.drawImageWithAnchorOld(SharkUtilities.rotateImage(ship, playerAngle), playerXint + (int)(Math.sin(playerAngle / Math.PI / 17.5)*4) - (int)cameraX - (int)((SharkUtilities.rotateImageToReturnWidth(ship, playerAngle) - 20) / 1), playerYint + (int)(Math.cos(playerAngle / Math.PI / 17.5)*4) - (int)cameraY - (int)((SharkUtilities.rotateImageToReturnHeight(ship, playerAngle) - 20) / 1), 0, 0.5, 0.5, g);
			} 
			default: {
				if (type!=1) {
				SharkUtilities.drawImageWithAnchorOld(SharkUtilities.rotateImage(player_2, playerAngle), playerXint - (int)cameraX - (int)((SharkUtilities.rotateImageToReturnWidth(player, playerAngle) - 20) / 1), playerYint - (int)cameraY - (int)((SharkUtilities.rotateImageToReturnHeight(player, playerAngle) - 20) / 1), 0, 0.5, 0.5, g);
				SharkUtilities.drawImageWithAnchorOld(SharkUtilities.rotateImage(player, playerAngle), playerXint - (int)cameraX - (int)((SharkUtilities.rotateImageToReturnWidth(player, playerAngle) - 20) / 1), playerYint - (int)cameraY - (int)((SharkUtilities.rotateImageToReturnHeight(player, playerAngle) - 20) / 1), 0, 0.5, 0.5, g);
			}}
		}
	}
}
