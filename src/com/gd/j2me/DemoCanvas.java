package com.gd.j2me;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

public class DemoCanvas extends GameCanvas implements Runnable {

	private boolean isRunning;
	private Thread gameThread;
	
	private boolean isTouching;	

	public DemoCanvas() {
        super(true);
        setFullScreenMode(true);
		// TODO Auto-generated constructor stub
	}
	
	public void start() {
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
	}
	
	public void stop() {
        isRunning = false;
	}
	

	public void run() {
		// TODO Auto-generated method stub
		while (isRunning) {
			update();
			draw();
			try { Thread.sleep(1); } catch (InterruptedException e) {}
		}
	}
	private void update() {}
	
	private void draw() {
		Graphics g = getGraphics();
		
		g.setColor(0x808080);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		g.setColor(0xffffff);
		g.drawString("Sorry, we erased all scripts", 0, 0, 0);
		g.drawString("but we're still working with the rewrite :)", 0, 12, 0);
		g.drawString("Btw, you can use \"old\" branch", 0, 36, 0);
		g.drawString("and run the latest build from the repo!", 0, 48, 0);
		
		flushGraphics();
	}
}
