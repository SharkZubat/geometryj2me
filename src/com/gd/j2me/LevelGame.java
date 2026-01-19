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

import com.tsg.hitbox.Direction;
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
	private static GameObject[] gobjtest = new GameObject[5000];
	public int objsize = 1;
	private Player music;
	PlayerScript curr_player = new PlayerScript();
	
	//camera
	float cameraX = 0;
	private float cameraY = 70;
	private int objcount = 0;
	
	private long lastFpsCheck = System.currentTimeMillis();
	private int currentFrames = 0;
	private int framesPerSecond = 0;
	private boolean isHolding;


	protected LevelGame(String levelData) {
		super(true);
		setFullScreenMode(true);
		// TODO Auto-generated constructor stub
		this.levelData = levelData;
	}

	public void start() {
		// TODO Auto-generated method stub
	    cameraX=curr_player.position.x+(getWidth()/6);
		String[] input = GameObject.getImages();
		for (int i = 0; i < input.length; i++) {
			try {
			    LevelLoader.Load(levelData); // Pass the filename
			} catch (IOException e1) {
			    e1.printStackTrace();
			}
		}
		
		//gobjtest = new GameObject[objsize];
		try {
			LevelLoader.Load(levelData);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			objImage[0] = Image.createImage("/img/obj/square_01_001.png");
			bigFontBig = Image.createImage("/img/fonts/bigFont-24.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
	        InputStream is = getClass().getResourceAsStream("/sounds/midi/Jumper.mid");
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
	
	public void resetdelta() {
	    lastFrameTime = System.currentTimeMillis();
		currentFrameTime = System.currentTimeMillis();
	    deltaTimeMillis = currentFrameTime - lastFrameTime;
	    deltaTimeSeconds = deltaTimeMillis / 1000.0;
	}
	
	public void addobj(GameObject data) {
		gobjtest[objcount] = data;
		objcount++;
	}
	
	public void stop() {
		// TODO Auto-generated method stub
		isRunning = false;
		try {
			music.stop();
		} catch (MediaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run() {
		// TODO Auto-generated method stub
	    long lastTime = System.currentTimeMillis();
	    final int TPS = 240;
	    final int SKIP_TICKS = 1000 / TPS;
	    long nextGameTick = System.currentTimeMillis();
		while (isRunning) {
	        long currentTime = System.currentTimeMillis();
	        long deltaTime = currentTime - lastTime;
	        lastTime = currentTime;
			controlcamera();
			while (currentTime > nextGameTick) {
				curr_player.update(SKIP_TICKS / 1000.0f, isHolding);
				nextGameTick += SKIP_TICKS;
				currentTime = System.currentTimeMillis();
			}
			update();
			draw();
			updatefps();
			flushGraphics();
	        try {
	            Thread.sleep(Math.max(0, nextGameTick - currentTime));
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
		}
	}
	
	private void controlcamera() {
		// TODO Auto-generated method stub
	    int keyState = getKeyStates();

	    //if ((keyState & LEFT_PRESSED) != 0) {
	    //    cameraX-=deltaTimeSeconds*256f;
	    //}
	    //if ((keyState & RIGHT_PRESSED) != 0) {
	    //	cameraX+=deltaTimeSeconds*256f;
	    //}
	    if ((keyState & DOWN_PRESSED) != 0) {
	    	cameraY+=deltaTimeSeconds*256f;
	    }
	    if ((keyState & UP_PRESSED) != 0) {
	    	cameraY-=256f*deltaTimeSeconds;
	    }
	}
	public void freeup() {
		SharkUtilities.clearCaches();
		System.gc();
	}
	
    private void update() {
	    int keyState = getKeyStates();
    	currentFrameTime = System.currentTimeMillis();
        deltaTimeMillis = currentFrameTime - lastFrameTime;
        deltaTimeSeconds = deltaTimeMillis / 1000.0;
        
        drewlayers = 0;
        dirTest += -60 * deltaTimeSeconds;
        
        if (Runtime.getRuntime().freeMemory()/1024 <= Runtime.getRuntime().totalMemory()/1024/2) {
        	freeup();
        }
        
	    if (((keyState & FIRE_PRESSED) != 0) || ((keyState & KEY_NUM5) != 0) || ((keyState & KEY_NUM2) != 0)) {
	    	isHolding = true;
	    } else {
	    	isHolding = false;
	    }
        
        lastFrameTime = currentFrameTime;
    }
    
    private void renderobject(Image[] obj, GameObject gobj) {
    	int id = gobj.id;
    	float x = gobj.x;
    	float y = gobj.y;
    	boolean h = gobj.h;
    	boolean v = gobj.v;
    	Direction dir = gobj.dir;
    	
    	long calculatedX = (long) Math.floor((x-cameraX)/1.3636363636363636363636363636363636f+(getWidth()/2));
    	long calculatedY = (long) Math.floor((-y+cameraY)/1.36363636363636363636363636363636f+(getHeight()/2));
    	//calculatedX -= (long)deltaTimeSeconds*deltaTimeSeconds;
    	if (calculatedX > -20
    			&& calculatedX < getWidth()+20
    			&& calculatedY > -20
    			&& calculatedY < getHeight()+20) {
	    	if (drewlayers <= 100) {
		    	Graphics g = getGraphics();
		    	if (dir.toFloat() == 0) {
		    		SharkUtilities.drawImageWithAnchor(obj[id], (int)calculatedX, (int)calculatedY, 0, 0.5, 0.5, g);
		    	} else {
		    		SharkUtilities.drawImageWithDirAnchor(obj[id], dir.toFloat(), (int)calculatedX, (int)calculatedY, 0, 0.5, 0.5, g);
		    	}
		    	drewlayers++;
	    	}
    	}
    }
    
    private void updatefps() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFpsCheck >= 1000) {
            framesPerSecond = (int) (currentFrames);
            currentFrames = 0;
            lastFpsCheck = currentTime;
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
		
		for (int i = 0; i < objsize; i++) {
			renderobject(objImage,gobjtest[i]);
		}
		renderobject(objImage,new GameObject(1,curr_player.position.x,curr_player.position.y,false,false,curr_player.dir));
		
		CustomFont.drawString(bigFontBig, 0, 48, 0.5f, "FPS: " + (int)(framesPerSecond), 22, g);
		CustomFont.drawString(bigFontBig, 0, 60, 0.5f, "Drawn layers: " + drewlayers, 22, g);
		//CustomFont.drawString(bigFontBig, 0, 72, 0.5f, "RAM: " + Runtime.getRuntime().freeMemory()/1024 + "KB/" + Runtime.getRuntime().totalMemory()/1024 + "KB", 22, g);
		//CustomFont.drawString(bigFontBig, 0, 86, 0.5f, "CamX: " + (int)cameraX + "CamY:" + (int)cameraY, 22, g);
		currentFrames++;
	}
}
