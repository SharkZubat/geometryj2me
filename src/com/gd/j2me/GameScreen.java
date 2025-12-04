package com.gd.j2me;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.Sprite;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import java.io.InputStream;
import java.io.IOException;
//import java.util.*;

public class GameScreen extends GameCanvas implements Runnable {
	private Launcher midlet;
    private boolean isRunning;
    private boolean isTouchingDown;
    private Thread gameThread;
    private float playerX = 0;
    private float playerY = 0;
    private float velocityY = 0;
    private float progress = 0f;
    private int progressFixed = 0;
    private float progressTest = 0; //for testing only
    private final float GRAVITY = 5.5f;
    private final float JUMP_STRENGTH = -22.36f;
    private final float GROUND_LEVEL = 0;
    
    //converted int variables
    private int playerXint = 0;
    private int playerYint = 0;
    private int GROUND_LEVELint = 0;
    
    //for deltatime
    private long lastFrameTime = System.currentTimeMillis();
	private long currentFrameTime = System.currentTimeMillis();
    private long deltaTimeMillis = currentFrameTime - lastFrameTime;
    private double deltaTimeSeconds = deltaTimeMillis / 1000.0;
    
    //ui variables
    private boolean isPaused = false;
    private Image pauseBtn;
    
    //music variables
    private Player midiPlayer;
    private long currentMidiTime;
    
    //game level variables
    private int gameVerID = 1;
    private String levelName = "Level";
    private int bgColor = 0x287DFF;
    private int gnColor = 0x287DFF;
    //private List obj;
    
    //camera variables
    private float cameraX = 0;
    private float cameraY = -159;
	private boolean isGrounded;
	
	//player variables
	private int type = 0;
	private Image player;
    
    public GameScreen(Launcher midlet) {
        super(true);
        this.midlet = midlet;
        setFullScreenMode(true);
    }

    public void start() {
        isRunning = true;
        
        try {
	        InputStream is = getClass().getResourceAsStream("/sounds/midi/Jumper.mid");
	        midiPlayer = Manager.createPlayer(is, "audio/midi");
	        midiPlayer.start();
	        is.close();
	        is = null;
        } catch (Exception e) {System.out.println("midi error:" + e);}
        
        try {
        	pauseBtn = Image.createImage("/img/GJ_pauseBtn_clean_001.png");
        	player = Image.createImage("/img/cube1.png");
        } catch (IOException ioex) {System.out.println("error:" + ioex);}
        
        deltaTimeMillis = 0;
        deltaTimeSeconds = 0;
        progress = 0;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void stop() {
        isRunning = false;
    	if (midiPlayer != null) {
	    	try {
				midiPlayer.stop();
			} catch (MediaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
    }

    public void run() {
        while (isRunning) {
            update();
            if (!isPaused) {
            	playerupdate();
            }
            draw();
            
            try { Thread.sleep(1); } catch (InterruptedException e) {}
        }
    }
    
    protected void pointerPressed(int x, int y) {
    	if (x >= 296 && x <= 316 && y >= 4 && y <= 24) {
    		System.out.println("pause button holding");
    	} else {
    		isTouchingDown = true;
    	}
    }
    
    private void pause() {
    	isPaused = !isPaused;
    	
    	if (isPaused) {
    		try {
				midiPlayer.stop();
				currentMidiTime = midiPlayer.getMediaTime();
			} catch (MediaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	} else {
    		try {
				midiPlayer.start();
				midiPlayer.setMediaTime(currentMidiTime);
			} catch (MediaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	}
    }
    
    protected void pointerReleased(int x, int y) {
    	if (x >= 296 && x <= 316 && y >= 4 && y <= 24) {
    		System.out.println("pause button pressed");
    		pause();
    	}
    	isTouchingDown = false;
    }

    private void update() {
    	currentFrameTime = System.currentTimeMillis();
        deltaTimeMillis = currentFrameTime - lastFrameTime;
        deltaTimeSeconds = deltaTimeMillis / 1000.0;
        
        lastFrameTime = currentFrameTime;
    }
    
    private void playerupdate() {
        int keyStates = getKeyStates();
        if ((keyStates & FIRE_PRESSED) != 0 || (keyStates & UP_PRESSED) != 0 || (isTouchingDown)) {
            if (isGrounded) {
                velocityY = JUMP_STRENGTH;
            }
        }
        
        progress += 1 * deltaTimeSeconds;
        progressFixed = (int) Math.floor(progress);
        progressTest = deltaTimeMillis;
        
        if (!(velocityY > (0 - JUMP_STRENGTH))) {
        } else {
        	
        }
        playerY += velocityY * deltaTimeSeconds * 20;
        playerX += 10.41667 * deltaTimeSeconds * 20;
        if (playerX > getWidth() * 0.3f) {
        	cameraX = playerX - (getWidth() * 0.3f);
        } else {
        	cameraX = 0;
        }
        if (playerY < GROUND_LEVEL) {
        	isGrounded = false;
            if (!(velocityY > (0 - JUMP_STRENGTH))) {
            	velocityY += GRAVITY * deltaTimeSeconds * 20;
            } else {
            	velocityY = 0 - JUMP_STRENGTH;
            }
        } else {
            playerY = playerY - (playerY - GROUND_LEVEL);
            velocityY = 0;
            isGrounded = true;
        }
        
        playerXint = (int)playerX;
        playerYint = (int)playerY;
        GROUND_LEVELint = (int)GROUND_LEVEL;
    }

    private void draw() {
        Graphics g = getGraphics();
        
        g.setColor(bgColor);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.setColor(0xFFFFFF);
        g.drawLine(0, (int) GROUND_LEVELint + 20 - (int)cameraY, getWidth(), (int) GROUND_LEVELint + 20 - (int)cameraY);
        
        g.setColor(0xFFFF00);
        g.drawImage(player, playerXint - (int)cameraX, playerYint - (int)cameraY, 0);
        
        //
        TextUtilities.setFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE, g);
        TextUtilities.drawOutlinedString("Attempt 1", (getWidth() / 2)+((0-(int)cameraX)-("Attempt 1".length() * 5)), getHeight() / 3, 0, g, 0xFFFFFF, 0x000000);
        
        //
        
        g.drawImage(pauseBtn, getWidth() - 24, 4, 0);
        
        String progressFixedStr = Integer.toString(progressFixed);
        
        g.setColor(0xFFFFFF);
        TextUtilities.setFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE, g);
        TextUtilities.drawOutlinedString(progressFixed + "%", (getWidth() / 2) - (progressFixedStr.length() * 5), 0, 0, g, 0xFFFFFF, 0x000000);
        
        if (isPaused) {
        	//
        	TextUtilities.drawOutlinedString(levelName, 0, 0, 0, g, 0xffffff, 0x000000);
        }
        
        String debugStr = "gameVerID:"+gameVerID+"\nlevelName:"+levelName+"\nbgColor:"+bgColor+"\ngnColor:"+gnColor;
        String debugStr2 = "\npyvel:"+velocityY+"\ncx:"+cameraX+"\ndtms:"+deltaTimeMillis;
        
        TextUtilities.setFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL, g);
        TextUtilities.drawOutlinedString(debugStr, 0, 0, 0, g, 0xFFFFFF, 0x000000);
        TextUtilities.drawOutlinedString(debugStr2, 0, 12, 0, g, 0xFFFFFF, 0x000000);
        
        flushGraphics();
    }
}