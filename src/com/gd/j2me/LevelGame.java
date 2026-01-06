package com.gd.j2me;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;

public class LevelGame extends GameCanvas implements Runnable {
	private String levelData = "";
	private boolean isRunning;
	private Thread gameThread;
	
	//game level settings
    private int bgColor = 0x287dff;
    private int gnColor = 0x0066ff;

	protected LevelGame(String levelData) {
		super(true);
		setFullScreenMode(true);
		// TODO Auto-generated constructor stub
		this.levelData = levelData;
	}

	public void start() {
		// TODO Auto-generated method stub
		isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
	}
	
	public void run() {
		// TODO Auto-generated method stub
		while (isRunning) {
			draw();
			flushGraphics();
		}
	}

	public void stop() {
		// TODO Auto-generated method stub
		isRunning = false;
	}
	
	private void draw() {
		Graphics g = getGraphics();
		
		g.setColor(bgColor);
		g.fillRect(0, 0, getWidth(), getHeight());
	}
}
