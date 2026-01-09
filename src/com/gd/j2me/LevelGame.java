package com.gd.j2me;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import javax.microedition.midlet.MIDlet;

import com.tsg.sharkutilitiesdemo.SharkUtilities;

import java.io.InputStream;
import java.io.IOException;

public class LevelGame extends GameCanvas implements Runnable {
    private long lastFrameTime = System.currentTimeMillis();
	public long currentFrameTime = System.currentTimeMillis();
    private long deltaTimeMillis = currentFrameTime - lastFrameTime;
    private double deltaTimeSeconds = deltaTimeMillis / 1000.0;
	
	private String levelData = "";
	public boolean isRunning;
	private Thread gameThread;
	
	//game level settings
    private int bgColor = 0x287dff;
    private int gnColor = 0x0066ff;
    
    //game level res
    private Image[] objImage = new Image[10];
    private Image bigFontBig;
	private int drewlayers;
	private float dirTest;
	private PlayerScript player = new PlayerScript();
	private GameObject[] gobjtest = new GameObject[40];
	private Player music;
	
	//camera
	private float cameraX = 0;
	private float cameraY = 0;

	protected LevelGame(String levelData) {
		super(true);
		setFullScreenMode(true);
		// TODO Auto-generated constructor stub
		this.levelData = levelData;
	}

	public void start() {
		// TODO Auto-generated method stub
		String[] input = GameObject.getImages();
		for (int i = 0; i < input.length; i++) {
			try {
				objImage[i] = Image.createImage("/img/obj/" + input[i]);
				System.out.println("initalized obj image:" + i);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		gobjtest[0] = new GameObject(1,0,0,false,false,0);
		try {
			objImage[0] = Image.createImage("/img/obj/square_01_001.png");
			bigFontBig = Image.createImage("/img/fonts/bigFont-24.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
	        InputStream is = getClass().getResourceAsStream("/sounds/midi/DryOut.mid");
	        music = Manager.createPlayer(is, "audio/midi");
	        music.prefetch();
	        music.start();
        	is.close();
        	is = null;
		} catch (MediaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			controlcamera();
			draw();
			flushGraphics();
			try { Thread.sleep(1); } catch (InterruptedException e) {}
		}
	}
	
	private void controlcamera() {
		// TODO Auto-generated method stub
	    int keyState = getKeyStates();

	    if ((keyState & LEFT_PRESSED) != 0) {
	        cameraX-=deltaTimeSeconds*256f;
	    }
	    if ((keyState & RIGHT_PRESSED) != 0) {
	    	cameraX+=deltaTimeSeconds*256f;
	    }
	    if ((keyState & UP_PRESSED) != 0) {
	    	cameraY+=deltaTimeSeconds*256f;
	    }
	    if ((keyState & DOWN_PRESSED) != 0) {
	    	cameraY-=256f*deltaTimeSeconds;
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
        
        if (Runtime.getRuntime().freeMemory()/1024 <= Runtime.getRuntime().totalMemory()/1024/2) {
        	freeup();
        }
        
        lastFrameTime = currentFrameTime;
    }
    
    private void renderobject(Image obj, GameObject gobj) {
    	int id = gobj.id;
    	float x = gobj.x;
    	float y = gobj.y;
    	boolean h = gobj.h;
    	boolean v = gobj.v;
    	float dir = gobj.dir;
    	
    	long calculatedX = (long) ((x-cameraX)/1.363636f+(getWidth()/2));
    	long calculatedY = (long) ((y+cameraY)/1.363636f+(getHeight()/2));
    	if (calculatedX > -20
    			&& calculatedX < getWidth()+20
    			&& calculatedY > -20
    			&& calculatedY < getHeight()+20) {
	    	if (drewlayers <= 100) {
		    	Graphics g = getGraphics();
		    	if (dir == 0) {
		    		SharkUtilities.drawImageWithAnchor(obj, (int)calculatedX, (int)calculatedY, 0, 0.5, 0.5, g);
		    	} else {
		    		SharkUtilities.drawImageWithDirAnchor(obj, dir, (int)calculatedX, (int)calculatedY, 0, 0.5, 0.5, g);
		    	}
		    	drewlayers++;
	    	}
    	}
    }
	
	private void draw() {
		Graphics g = getGraphics();
		
		g.setColor(bgColor);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		//for (int i = 0; i < 4; i++) {
		//	for (int j = 0; j < 2; j++) {
		//		renderobject(objImage[0],i*30,j*30,dirTest);
		//	}
		//}
		
		renderobject(objImage[0],gobjtest[0]);
		
		CustomFont.drawString(bigFontBig, 0, 48, 0.5f, "FPS: " + (int)(1f/deltaTimeSeconds) + "/" + deltaTimeSeconds, 22, g);
		CustomFont.drawString(bigFontBig, 0, 60, 0.5f, "Drawn layers: " + drewlayers, 22, g);
		CustomFont.drawString(bigFontBig, 0, 72, 0.5f, "RAM: " + Runtime.getRuntime().freeMemory()/1024 + "KB/" + Runtime.getRuntime().totalMemory()/1024 + "KB", 22, g);
	}
}
