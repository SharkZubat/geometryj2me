package com.gd.j2me;

import java.io.IOException;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;

import com.tsg.sharkutilitiesdemo.SharkUtilities;

public class LevelGame extends GameCanvas implements Runnable {
    private long lastFrameTime = System.currentTimeMillis();
	private long currentFrameTime = System.currentTimeMillis();
    private long deltaTimeMillis = currentFrameTime - lastFrameTime;
    private double deltaTimeSeconds = deltaTimeMillis / 1000.0;
	
	private String levelData = "";
	private boolean isRunning;
	private Thread gameThread;
	
	//game level settings
    private int bgColor = 0x287dff;
    private int gnColor = 0x0066ff;
    
    //game level res
    private Image objImage;
    private Image bigFontBig;
	private int drewlayers;
	private float dirTest;

	protected LevelGame(String levelData) {
		super(true);
		setFullScreenMode(true);
		// TODO Auto-generated constructor stub
		this.levelData = levelData;
	}

	public void start() {
		// TODO Auto-generated method stub
		try {
			objImage = Image.createImage("/img/obj/square_01_001.png");
			bigFontBig = Image.createImage("/img/fonts/bigFont-24.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
	}
	
	public void stop() {
		// TODO Auto-generated method stub
		isRunning = false;
	}
	
	public void run() {
		// TODO Auto-generated method stub
		while (isRunning) {
			update();
			draw();
			flushGraphics();
			try { Thread.sleep(1); } catch (InterruptedException e) {}
		}
	}
	
	public void freeup() {
		SharkUtilities.clearCaches();
		System.gc();
	}
	
    private void update() {
    	currentFrameTime = System.currentTimeMillis();
        deltaTimeMillis = currentFrameTime - lastFrameTime;
        deltaTimeSeconds = deltaTimeMillis / 1000.0;
        
        drewlayers = 0;
        dirTest += -60 * deltaTimeSeconds;
        
        if (Runtime.getRuntime().freeMemory()/1024 <= 1024) {
        	freeup();
        }
        
        lastFrameTime = currentFrameTime;
    }
    
    private void renderobject(Image obj, float x, float y, float dir) {
    	Graphics g = getGraphics();
    	if (dir == 0) {
    		g.drawImage(obj, (int)x, (int)y, 0);
    	} else {
    		SharkUtilities.drawImageWithDir(objImage, dir, x, y, 0, g);
    	}
    	drewlayers++;
    }
	
	private void draw() {
		Graphics g = getGraphics();
		
		g.setColor(bgColor);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		renderobject(objImage,0,0,dirTest);
		renderobject(objImage,22,0,dirTest);
		renderobject(objImage,44,0,dirTest);
		renderobject(objImage,0,22,dirTest);
		renderobject(objImage,22,22,dirTest);
		renderobject(objImage,44,22,dirTest);
		
		CustomFont.drawString(bigFontBig, 0, 48, 0.5f, "FPS: " + (int)(1f/deltaTimeSeconds), 22, g);
		CustomFont.drawString(bigFontBig, 0, 60, 0.5f, "Drawn Layers: " + drewlayers, 22, g);
		CustomFont.drawString(bigFontBig, 0, 72, 0.5f, "RAM: " + Runtime.getRuntime().freeMemory()/1024 + "KB/" + Runtime.getRuntime().totalMemory() + "KB", 22, g);
	}
}
