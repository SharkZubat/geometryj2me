package com.gd.j2me;

import javax.microedition.midlet.MIDlet;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.*;

public class Launcher extends MIDlet {
	private Display display;
    private MainMenu mainMenu;
	private GameScreen gameScreen;
    
    public void startApp() {
        //if (mainMenu == null) {
    	display = Display.getDisplay(this);
        mainMenu = new MainMenu(this);
        mainMenu.start();
        //}
        Display.getDisplay(this).setCurrent(mainMenu);
    }

    public void pauseApp() {
        //nothing
    }

    public void destroyApp(boolean unconditional) {
        if (mainMenu != null) {
            mainMenu.stop();
        }
    }
    
    public void switchDisplay(Displayable nextDisplayable) {
        display.setCurrent(nextDisplayable);
    }
    
    public GameScreen getGameScreen() {
    	mainMenu = null;
    	gameScreen = new GameScreen(this);
    	gameScreen.start();
    	return gameScreen;
    }
}