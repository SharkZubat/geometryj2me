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

public class LoadingScreen extends GameCanvas implements Runnable {
	
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
	private boolean isTouchingDown;
	private String selection;
	private Image bigFont;
	private Image bigFontbig;
	private Image goldFont;
	private Image goldFontbig;
	private String menu = "main";
	
	//Parallax ground
	private Image ground;
	private Image gnshadow;
	private Image lineimage;
	private Image groundRainbow;
	private float groundX = 0;
	private float groundSpeed = 10.41667f;
	//colours
	private float hue = 0f;        
	private float hueSpeed = 0.025f; 
	private int rainbowColor = 0x287DFF;
    
    public LoadingScreen(Launcher midlet) {
        super(false);
        this.midlet = midlet;
        setFullScreenMode(true);
    }
    
    public void start() {	
    	isRunning = true;
    	
        try {
        	logo = Image.createImage("/img/GJ_logo_001.png");
        	ground = Image.createImage("/img/level/groundSquare_01_001.png");
        	bigFont = Image.createImage("/img/fonts/bigFont.png");
        	bigFontbig = Image.createImage("/img/fonts/bigFont-24.png");
        	goldFont = Image.createImage("/img/fonts/goldFont.png");
        	goldFontbig = Image.createImage("/img/fonts/goldFont-24.png");
        	lineimage = SharkUtilities.tintImage(Image.createImage("/img/level/floorLine_01_001.png"), 0xffffff);
        	gnshadow = SharkUtilities.tintImage(Image.createImage("/img/level/groundSquareShadow_001.png"), 0xffffff);
        } catch (IOException ioex) {System.out.println("error:" + ioex);}
        drawloading();
        
        gameThread = new Thread(this);
        gameThread.start();
    }
    
    public void stop() {
    	isRunning = false;
    }
    
    
    public void run() {
    	while (isRunning) {
            update();
            drawloading();
            
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

        rainbowColor = SharkUtilities.hsvToRgb(hue+0.6f, 0.84f, 1f);
        //
        int keyStates = getKeyStates();
        if (selection == "playBtn") {
        	menu = "mainlevels";
        	selection = null;
        }
        
        lastFrameTime = currentFrameTime;
    }
    public void drawloading() {
    	Graphics g = getGraphics();
    	
        g.setColor(0x287DFF);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        TextUtilities.setFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_LARGE, g);
        TextUtilities.drawOutlinedString("Loading Resources", 0, 0, 0, g, 0xffdd00, 0x000000);

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
    
    public void drawmainlevels() {
    	Graphics g = getGraphics();
    	
        g.setColor(0x287DFF);
        g.fillRect(0, 0, getWidth(), getHeight());
        CustomFont.drawString(bigFontbig, 0, 0, 0.5f, "Test Level", 22, g);
        
        flushGraphics();
    }
}
