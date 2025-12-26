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
	private Image bigFont;
	private Image bigFontbig;
	private String menu = "main";
    
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
        	bigFont = Image.createImage("/img/fonts/bigFont.png");
        	bigFontbig = Image.createImage("/img/fonts/bigFont-24.png");
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
            
            try { Thread.sleep(0); } catch (InterruptedException e) {}
    	}
    }
    
    public void update() {
    	long currentFrameTime = System.currentTimeMillis();
        long deltaTimeMillis = currentFrameTime - lastFrameTime;
        double deltaTimeSeconds = deltaTimeMillis / 1000.0;
        //
        int keyStates = getKeyStates();
        if (selection == "playBtn") {
        	menu = "mainlevels";
        	selection = null;
        }
        //
        lastFrameTime = currentFrameTime;
    }
    
    public void draw() {
    	Graphics g = getGraphics();
    	
        g.setColor(0x287DFF);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.drawImage(logo, (int) ((logo.getWidth() - getWidth()) * -0.5f), 10, 0);
        g.drawImage(playBtn, (int) ((playBtn.getWidth() - getWidth()) * -0.5f), (int) ((playBtn.getHeight() - getHeight()) * -0.5f)-10, 0);
        g.drawImage(garageBtn, (int) ((garageBtn.getWidth() - getWidth()) * -0.2f), (int) ((garageBtn.getHeight() - getHeight()) * -0.5f)-10, 0);
        g.drawImage(creatorBtn, (int) ((creatorBtn.getWidth() - getWidth()) * -0.8f), (int) ((creatorBtn.getHeight() - getHeight()) * -0.5f)-10, 0);
        
        
        flushGraphics();
    }
    
    public void drawmainlevels() {
    	Graphics g = getGraphics();
    	
        g.setColor(0x287DFF);
        g.fillRect(0, 0, getWidth(), getHeight());
        CustomFont.drawString(bigFontbig, 0, 0, 1f, "test", 22, g);
        
        flushGraphics();
    }
}
