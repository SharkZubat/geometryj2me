package com.gd.j2me;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

public class Launcher extends MIDlet {
	private Display display;
    private DemoCanvas democanvas;
    
    public void startApp() {
    	display = Display.getDisplay(this);
        democanvas = new DemoCanvas();
        democanvas.start();
        Display.getDisplay(this).setCurrent(democanvas);
    }

    public void pauseApp() {
        //nothing
    }

    public void destroyApp(boolean unconditional) {
        if (democanvas != null) {
            democanvas.stop();
        }
    }
    
    public void switchDisplay(Displayable nextDisplayable) {
        display.setCurrent(nextDisplayable);
    }

}