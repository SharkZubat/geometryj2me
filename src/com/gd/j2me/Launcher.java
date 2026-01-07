package com.gd.j2me;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

public class Launcher extends MIDlet {
	private Display display;
    private LevelGame levelGame;
	private boolean isRunning;
    
    public void startApp() {
    	if (!isRunning) {
	    	isRunning = true;
	    	display = Display.getDisplay(this);
	        levelGame = new LevelGame("tyest");
	        levelGame.start();
	        Display.getDisplay(this).setCurrent(levelGame);
    	}
    }

    public void pauseApp() {
        // todo: make to pause, not returning to beginning one
    	
    }

    public void destroyApp(boolean arg0) {
    	isRunning = false;
        if (levelGame != null) {
        	levelGame.stop();
        }
    }
    
    public void switchDisplay(Displayable nextDisplayable) {
        display.setCurrent(nextDisplayable);
    }

}