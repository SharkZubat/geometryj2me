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
import java.io.InputStream;
import java.io.IOException;

public class MainMenu extends GameCanvas implements Runnable {
	
    private boolean isRunning;
    private Thread gameThread;
    
    private long lastFrameTime = System.currentTimeMillis();
	private long currentFrameTime = System.currentTimeMillis();
    private long deltaTimeMillis = currentFrameTime - lastFrameTime;
    private double deltaTimeSeconds = deltaTimeMillis / 1000.0;
    private GameScreen gameScreen;
    private Launcher midlet;
    
    //ui variables
    private Image logo;
	private Image playBtn;
	private Image garageBtn;
	private Image creatorBtn;
	private boolean isTouchingDown;
	private String selection;
	private Player midiPlayer;
	private Player sfx;
	private Image bigFont;
	private Image bigFontbig;
	private String menu = "main";
	
	//Parallax ground
	private Image ground;
	private float groundX = 0;
	private float groundSpeed = 10.41667f;
	//colours
	private float hue = 0f;        
	private float hueSpeed = 0.025f; 
	private int rainbowColor = 0x287DFF;
	private Image gnshadow;
	private Image lineimage;
	private Image groundRainbow;

    
    public MainMenu(Launcher midlet) {
        super(true);
        this.midlet = midlet;
        setFullScreenMode(true);
    }
    
    public void start() {	
    	isRunning = true;
    	
        try {
	        InputStream is = getClass().getResourceAsStream("/sounds/midi/mainLoop.mid");
	        midiPlayer = Manager.createPlayer(is, "audio/midi");
	        midiPlayer.setLoopCount(-1);
	        midiPlayer.start();
	        is.close();
	        is = null;
        } catch (Exception e) {System.out.println("midi error:" + e);}
        try {
        	logo = Image.createImage("/img/GJ_logo_001.png");
        	playBtn = Image.createImage("/img/GJ_playBtn_001.png");
        	garageBtn = Image.createImage("/img/GJ_garageBtn_001.png");
        	creatorBtn = Image.createImage("/img/GJ_creatorBtn_001.png");
        	ground = Image.createImage("/img/level/groundSquare_01_001.png");
        	bigFont = Image.createImage("/img/fonts/bigFont.png");
        	bigFontbig = Image.createImage("/img/fonts/bigFont-24.png");
        	lineimage = SharkUtilities.tintImage(Image.createImage("/img/level/floorLine_01_001.png"), 0xffffff);
        	gnshadow = SharkUtilities.tintImage(Image.createImage("/img/level/groundSquareShadow_001.png"), 0xffffff);
        } catch (IOException ioex) {System.out.println("error:" + ioex);}
        
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
    
    protected void pointerPressed(int x, int y) {
    	if (x >= ((int) ((playBtn.getWidth() - getWidth()) * -0.5f)) && x <= ((int) ((playBtn.getWidth() - getWidth()) * -0.5f))+playBtn.getWidth() && y >= ((int) ((playBtn.getHeight() - getHeight()) * -0.5f)-10) && y <= ((int) ((playBtn.getHeight() - getHeight()) * -0.5f)-10)+playBtn.getHeight()) {
    		System.out.println("play button holding");
    	}
    	isTouchingDown = true;
    }
    
    protected void pointerReleased(int x, int y) {
    	if (x >= ((int) ((playBtn.getWidth() - getWidth()) * -0.5f)) && x <= ((int) ((playBtn.getWidth() - getWidth()) * -0.5f))+playBtn.getWidth() && y >= ((int) ((playBtn.getHeight() - getHeight()) * -0.5f)-10) && y <= ((int) ((playBtn.getHeight() - getHeight()) * -0.5f)-10)+playBtn.getHeight()) {
    		System.out.println("play button pressed");
    		selection = "playBtn";
    	}
    	isTouchingDown = false;
    }
    
    protected void keyPressed(int keyCode) {
    }
    
    protected void keyReleased(int keyCode) {
    	int gameAction = getGameAction(keyCode);
        if (gameAction == FIRE && menu == "mainlevels") {
        	System.out.println("test");
        	this.stop();
        	SharkUtilities.playWAV("/sounds/wav/playSound_01.wav", getClass(), sfx);
        	midlet.switchDisplay(midlet.getGameScreen());
        	selection = null;
        }
        if (gameAction == Canvas.FIRE) {
        	menu = "mainlevels";
        	selection = null;
        }
    }
    
    
    public void run() {
    	while (isRunning) {
            update();
            if (menu == "main") {
            	draw();
            } else if (menu == "mainlevels") {
            	drawmainlevels();
            }
            
            try { Thread.sleep(1); } catch (InterruptedException e) {}
    	}
    }
    
    public void update() {
    	long currentFrameTime = System.currentTimeMillis();
        long deltaTimeMillis = currentFrameTime - lastFrameTime;
        double deltaTimeSeconds = deltaTimeMillis / 1000.0;
        
        // scroll ground
        groundX += (groundSpeed * deltaTimeSeconds * 20);
        groundRainbow = SharkUtilities.tintImage(ground, rainbowColor);

        if (groundX <= -ground.getWidth()) {
            groundX += ground.getWidth();
        }
        hue += hueSpeed * deltaTimeSeconds;
        if (hue > 1f) hue -= 1f;

        rainbowColor = hsvToRgb(hue+0.6f, 0.84f, 1f);
        //
        int keyStates = getKeyStates();
        if (selection == "playBtn") {
        	menu = "mainlevels";
        	selection = null;
        }
        
        lastFrameTime = currentFrameTime;
    }
    
    public void draw() {
    	Graphics g = getGraphics();
    	
        g.setColor(rainbowColor);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        //button ui
        g.drawImage(logo, (int) ((logo.getWidth() - getWidth()) * -0.5f), 10, 0);
        g.drawImage(playBtn, (int) ((playBtn.getWidth() - getWidth()) * -0.5f), (int) ((playBtn.getHeight() - getHeight()) * -0.5f)-10, 0);
        g.drawImage(garageBtn, (int) ((garageBtn.getWidth() - getWidth()) * -0.2f), (int) ((garageBtn.getHeight() - getHeight()) * -0.5f)-10, 0);
        g.drawImage(creatorBtn, (int) ((creatorBtn.getWidth() - getWidth()) * -0.8f), (int) ((creatorBtn.getHeight() - getHeight()) * -0.5f)-10, 0);
        
        int groundY = getHeight() - ground.getHeight() + 20;

        //draw ground
        renderground(g, groundY, false);
        flushGraphics();
    }
    
    private void renderground(Graphics g, float y, boolean isUpsideDown) {
    	if (!isUpsideDown) {
        	for (int i = 0; i-75 < 320; i += 75) {
            	SharkUtilities.drawImageWithAnchor(groundRainbow, (int) -groundX+i+(int) (Math.floor((double) (groundX/75))*75), (int) y, 0, 0.0, 0.0, g);
            }
            
            SharkUtilities.drawImageWithAnchor(gnshadow, 0, (int) y, 0, 0, 0, g);
            SharkUtilities.drawImageWithAnchor(SharkUtilities.flipImageHorizontal(gnshadow), getWidth(), (int) y, 0, -1, 0, g);
            
            SharkUtilities.drawImageWithAnchor(lineimage, getWidth() / 2, (int) y, 0, -0.5, 0, g);
    	} else {
        	for (int i = 0; i-75 < 320; i += 75) {
            	SharkUtilities.drawImageWithAnchor(SharkUtilities.flipImageVertical(groundRainbow), (int) -groundX+i+(int) (Math.floor((double) (groundX/75))*75), (int) y-75, 0, 0.0, 0.0, g);
            }
            
            SharkUtilities.drawImageWithAnchor(gnshadow, 0, (int) y-75, 0, 0, 0, g);
            SharkUtilities.drawImageWithAnchor(SharkUtilities.flipImageHorizontal(gnshadow), getWidth(), (int) y-75, 0, -1, 0, g);
            
            SharkUtilities.drawImageWithAnchor(lineimage, getWidth() / 2, (int) y-lineimage.getHeight(), 0, -0.5, 0, g);
    	}
    }
    
    private int hsvToRgb(float h, float s, float v) {
        int r = 0, g = 0, b = 0;

        int i = (int)(h * 6f);
        float f = h * 6f - i;
        float p = v * (1f - s);
        float q = v * (1f - f * s);
        float t = v * (1f - (1f - f) * s);

        switch (i % 6) {
            case 0: r = (int)(v * 255); g = (int)(t * 255); b = (int)(p * 255); break;
            case 1: r = (int)(q * 255); g = (int)(v * 255); b = (int)(p * 255); break;
            case 2: r = (int)(p * 255); g = (int)(v * 255); b = (int)(t * 255); break;
            case 3: r = (int)(p * 255); g = (int)(q * 255); b = (int)(v * 255); break;
            case 4: r = (int)(t * 255); g = (int)(p * 255); b = (int)(v * 255); break;
            case 5: r = (int)(v * 255); g = (int)(p * 255); b = (int)(q * 255); break;
        }

        return (r << 16) | (g << 8) | b;
    }
   
    
    public void drawmainlevels() {
    	Graphics g = getGraphics();
    	
        g.setColor(0x287DFF);
        g.fillRect(0, 0, getWidth(), getHeight());
        CustomFont.drawString(bigFontbig, 0, 0, 0.5f, "Test Level", 22, g);
        
        flushGraphics();
    }
}
