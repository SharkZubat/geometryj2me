package com.gd.j2me;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import java.util.Random;

import com.gd.j2me.SharkUtilities.Hitbox;

import java.io.InputStream;
import java.io.IOException;
import java.util.*;

public class GameScreen extends GameCanvas implements Runnable {
	private float istillrememberjava;
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
    private float GRAVITY = 1.25f;
    private final float JUMP_STRENGTH = -11.18f;
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
    private Image pausemenubg;
    
    //music variables
    private Player midiPlayer;
    private Player sfx;
    private long currentMidiTime;
    
    //game level variables
    private int gameVerID = 1;
    private String levelName = "Level";
    private int bgColor = 0x287dff;
    private int gnColor = 0x0066ff;
    private static int[] objid = new int[1000];
    private static float[] objx = new float[1000];
    private static float[] objy = new float[1000];
    private static int objlength = 0;
    private static Hitbox[] objlengthhitbox = new Hitbox[1000];
    private Image bgimage;
    private Image gnimage;
    private Image lineimage;
    private String music = "/sounds/midi/Jumper.mid";
    private Image gnshadow;
    private boolean isPlaying = false;
    private boolean isDone;
    private boolean isLoadingLevel = true;
    private static boolean[] objdataistouching = new boolean[1000];
    private int attempts = 1;
    
    //camera variables
    private float cameraX = 30;
    private float cameraY = -159-((getHeight()-240)/2);
    private int border = 0;
	private boolean isGrounded;
	
	//player variables
	private int type = 0;
	private boolean isFlipped;
	private Image player;
	private Image player_2;
	private Image player_glow;
	private SharkUtilities.Hitbox playerhitbox = SharkUtilities.Hitbox.rect(0f, 0f, 20f, 20f, 0.5, 0.5);
	private SharkUtilities.Hitbox smallphitbox = SharkUtilities.Hitbox.rect(0f, 0f, 5f, 5f, 0, 0);
	private boolean isTouched;
	private boolean isDeath;
	private Image[] objimage = new Image[100];
	private float playerAngle = 180;
	private boolean isTouched2;
	private Image bigFont;
	private Image bigFontbig;
	private boolean isFlying;
	private Image ship;
	private Vector trailPoints = new Vector();
    private final int MAX_TRAIL_LENGTH = 20;
	private boolean isRestartReady;
	private int countdownTrail;
	private boolean isTrailing;
	private static int[] objlengthimage = new int[100];
	private int color1 = 0x40ff00;
	private int color2 = 0x00ffff;
	//private SharkUtilities.Hitbox obj = SharkUtilities.Hitbox[5]
    
    public GameScreen(Launcher midlet) {
        super(true);
        this.midlet = midlet;
        setFullScreenMode(true);
    }

    public void start() throws IOException {
        isRunning = true;
        System.out.println(new Random().nextInt(3) + 9);
        
        try {
        	pauseBtn = Image.createImage("/img/GJ_pauseBtn_clean_001.png");
        	player = SharkUtilities.tintImage(Image.createImage("/img/icons/player_01_001.png"), color1);
        	player_2 = SharkUtilities.tintImage(Image.createImage("/img/icons/player_01_2_001.png"), color2);
        	ship = SharkUtilities.tintImage(Image.createImage("/img/icons/ship_01_001.png"), color1);
        	player_glow = Image.createImage("/img/icons/player_01_glow_001.png");
        	pausemenubg = SharkUtilities.scale(Image.createImage("/img/128black.png"), getWidth(), getHeight());
        	bgimage = SharkUtilities.tintImage(Image.createImage("/img/level/game_bg_01_001.png"), bgColor);
        	gnimage = SharkUtilities.tintImage(Image.createImage("/img/level/groundSquare_01_001.png"), gnColor);
        	lineimage = SharkUtilities.tintImage(Image.createImage("/img/level/floorLine_01_001.png"), 0xffffff);
        	gnshadow = SharkUtilities.tintImage(Image.createImage("/img/level/groundSquareShadow_001.png"), 0xffffff);
        	bigFont = Image.createImage("/img/fonts/bigFont.png");
        	bigFontbig = Image.createImage("/img/fonts/bigFont-24.png");
        } catch (IOException ioex) {System.out.println("error:" + ioex);}
        
		try {
			InputStream is = getClass().getResourceAsStream(music);
			midiPlayer = Manager.createPlayer(is, "audio/midi");
			is.close();
			is = null;
		} catch (Exception e) {System.out.println("midi error:" + e);}
        
        deltaTimeMillis = 0;
        deltaTimeSeconds = 0;
        progress = 0;
        cameraY = -159-((getHeight()-240));
        
        GameObject.create(1, 330, -10, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(1, 350, 30, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(7, 350, 10, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(1, 410, 10, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(7, 490, 10, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(1, 490, 30, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(7, 570, 10, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(7, 570, 30, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(1, 570, 50, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(7, 630, 10, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(1, 630, 30, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        //GameObject.create(8, 60, 50, objid, objx, objy, objlength, objlengthhitbox);
        //objlength++;
        //GameObject.create(8, 80, 50, objid, objx, objy, objlength, objlengthhitbox);
        //objlength++;
        //GameObject.create(8, 100, 50, objid, objx, objy, objlength, objlengthhitbox);
        //objlength++;
        //GameObject.create(8, 120, 50, objid, objx, objy, objlength, objlengthhitbox);
        //objlength++;
        //GameObject.create(8, 140, 50, objid, objx, objy, objlength, objlengthhitbox);
        //objlength++;
        GameObject.create(7, 690, 10, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(7, 690, 30, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(7, 690, 50, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(1, 690, 70, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(7, 750, 10, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(7, 750, 30, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(7, 750, 50, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(7, 750, 70, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(7, 750, 90, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(1, 750, 110, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(1, 830, 10, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(1, 850, 10, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(7, 910, 10, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(7, 910, 30, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(1, 910, 50, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(4, 1210, 70, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 1;
        objlength++;
        GameObject.create(5, 1410, 10, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 9;
        objlength++;
        GameObject.create(6, 1430, 10, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 9;
        objlength++;
        GameObject.create(7, 1450, 10, objid, objx, objy, objlength, objlengthhitbox);
        objlengthimage[objlength] = 9;
        objlength++;
        makenewobj(9, 370, 1.666666666666667f);
        makenewobj(9, 390, 1.666666666666667f);
        makenewobj(11, 1600, 30);
        makenewobj(1, 1600, 90);
        makenewobj(1, 1620, 90);
        makenewobj(1, 1640, 90);
        makenewobj(1, 1660, 90);
        makenewobj(1, 1680, 90);
        makenewobj(1, 1700, 90);
        makenewobj(1, 1720, 90);
        makenewobj(1, 1740, 90);
        makenewobj(1, 1760, 90);
        makenewobj(1, 1780, 90);
        makenewobj(10, 1820, 90);
        makenewobj(13, 1900, 30);
        //makenewobj(11, 1900, 30);
        makenewobj(12, 2100, 30);
        makenewobj(1, 2500, 110);
        makenewobj(1, 2500, 70);
        makenewobj(1, 2520, 110);
        makenewobj(1, 2520, 70);
        makenewobj(1, 2540, 110);
        makenewobj(1, 2540, 70);
        makenewobj(1, 2560, 110);
        makenewobj(1, 2560, 70);
        makenewobj(1, 2580, 110);
        makenewobj(1, 2580, 70);
        makenewobj(1, 2600, 110);
        makenewobj(14, 2600, 70);
        //makenewobj(14, 2620, 70);
        //makenewobj(14, 2640, 70);
        //makenewobj(14, 2660, 70);
        //makenewobj(14, 2680, 70);
        makenewobj(1, 2620, 110);
        makenewobj(1, 2640, 110);
        makenewobj(1, 2660, 110);
        makenewobj(1, 2680, 110);
       // makenewobj(10, 1620, 30);
        //makenewobj(10, 1640, 30);
        makenewobj(8, 2880, 10);
        LevelLoader.Load("let's Dih!");
        try {
        	Vector currentobjid = new Vector();

        	for (int i = 0; i < objid.length; i++) {
        	    String idString = String.valueOf(objid[i]);
        	    boolean alreadyExists = false;
        	    for (int j = 0; j < currentobjid.size(); j++) {
        	        if (currentobjid.elementAt(j).equals(idString)) {
        	            alreadyExists = true;
        	            break;
        	        }
        	    }
        	    if (!alreadyExists) {
        	        objimage[objid[i]] = GameObject.getImage(objid[i]);
        	        currentobjid.addElement(idString);
        	        System.out.println("Successfully added image for ID: " + objid[i]);
        	    }
        	}
        } catch (IOException e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
        }
        gameThread = new Thread(this);
        gameThread.start();
        if (!isDone) {
        	Timer aTimer= new Timer();
        	TimerTask ttask = new TimerTask() {
        		public void run() {
        			if (!isDone && !isPaused && isRunning) {
	        			try {
							midiPlayer.start();
						} catch (MediaException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
        			}
        			isDone = true;
        		}
        	};
        	isLoadingLevel = false;
			aTimer.schedule(ttask, 1000);
        }
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
    
    private void handletrails() {
    	if (isTrailing) {
    	if (countdownTrail == 0) {
    		trailPoints.addElement(new Point((int)playerhitbox.getX()+5, (int)playerhitbox.getY()+5));
    		countdownTrail = 2;
    	}
    	
    	countdownTrail -= 1;

        if (trailPoints.size() > MAX_TRAIL_LENGTH) {
            trailPoints.removeElementAt(0);
        }} else {
        	if (trailPoints.size() != 0) {
        		trailPoints.removeElementAt(0);
        	}
        }
    }
    
    private void rendertrails(Graphics g) {
        for (int i = 0; i < trailPoints.size(); i++) {
            Point p = (Point) trailPoints.elementAt(i);
            //int shade = (int) (255.0 * i / MAX_TRAIL_LENGTH);
            int transparent = ((int)(128 * i / MAX_TRAIL_LENGTH) << 24) | (255 << 16) | (255 << 8) | 255;
            g.setColor(transparent);

            SharkUtilities.fillRectWithTransp(p.x - (int)cameraX + 10, p.y - (int)cameraY+10, 10, 10, transparent, g);
        }
    }
    
    public static void makenewobj(int id, int x, float f) throws IOException {
        GameObject.create(id, x, f, objid, objx, objy, objlength, objlengthhitbox);
        objdataistouching[objlength] = false;
        objlengthimage[objlength] = id;
        objlength++;
    }
    
    //public void startgame() {
    //	if (!isPlaying) {
    //    	try {
    //			draw();
    //		} catch (IOException e2) {
    //			// TODO Auto-generated catch block
    //			e2.printStackTrace();
    //		}
    //		try { Thread.sleep(1000); } catch (InterruptedException e) {}
    //		isPlaying = true;
    //    	update();
    //   	try { Thread.sleep(1); } catch (InterruptedException e) {}
    //	}
    //}

    public void run() {
        while (isRunning) {
            update();
            if (!isPaused && isDone) {
            	playerupdate();
            }
            if (!isDone) {
            	playerX = 0;
            }
            try {
            	if (!isLoadingLevel) {
            		draw();
            	} else {
            		drawloading();
            	}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            
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
    	} else if (!isDeath) {
    		try {
    			if (!isRestartReady) {
					midiPlayer.start();
					midiPlayer.setMediaTime(currentMidiTime);
    			} else {
    				restart();
    			}
			} catch (MediaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    	} else {
			if (isRestartReady) {
				restart();
			}
    	}
    }
    
    protected void pointerReleased(int x, int y) {
    	if ((x >= 296 - (320-getWidth()) && x <= 316 - (320-getWidth()) && y >= 4 && y <= 24) && !isPaused) {
    		System.out.println("pause button pressed");
    		pause();
    	}
    	isTouchingDown = false;
    }
    
    protected void keyPressed(int keyCode) {
    	if (keyCode == -7 || getKeyName(keyCode).toUpperCase().indexOf("RIGHT") != -1) {
            // Action for the right soft button
            pause();
        }
    }

    private void update() {
    	currentFrameTime = System.currentTimeMillis();
        deltaTimeMillis = currentFrameTime - lastFrameTime;
        deltaTimeSeconds = deltaTimeMillis / 1000.0; 
        
        lastFrameTime = currentFrameTime;
    }
    
    private void checkjump() {
        int keyStates = getKeyStates();
        if ((keyStates & FIRE_PRESSED) != 0 || (keyStates & UP_PRESSED) != 0 || (isTouchingDown)) {
            if (isGrounded) {
            	if (!isFlipped) {
            		velocityY = (float) (JUMP_STRENGTH+GRAVITY * deltaTimeSeconds * 42);
            	} else {
            		velocityY = (float) (-JUMP_STRENGTH-GRAVITY * deltaTimeSeconds * 42);
            	}
                isGrounded = false;
                isTouched = false;
            }
        }
    }
    
    private void checkjumpaftertouch() {
        int keyStates = getKeyStates();
        if (((keyStates & FIRE_PRESSED) != 0 || (keyStates & UP_PRESSED) != 0 || (isTouchingDown)) && !isDeath && !isPaused) {
        	switch (type) {
        		case 0: {
        			if (type==0)
		            if (isGrounded) {
		            	if (!isFlipped) {
		            		velocityY = (float) (JUMP_STRENGTH+GRAVITY * deltaTimeSeconds * 42);
		            	} else {
		            		velocityY = (float) (-JUMP_STRENGTH-GRAVITY * deltaTimeSeconds * 42);
		            	}
		                isGrounded = false;
		                isTouched = false;
		            }
        		}
        		case 1: {
        			if (type!=1) {
        				break;
        			}
        			if (!isFlying) {
        		        if (!isFlipped) {
        		            velocityY += 1.f/8;
        		        } else {
        		        	velocityY += -1.f/8;
        		        }
        			}
        			isFlying = true;
        		}
        	}
        } else {
        	isFlying = false;
        }
    }
    
    private void checkothercollis() {
    	for (int i = 0; i < objlength; i++) {
        	if (SharkUtilities.Hitbox.isTouching(playerhitbox, objlengthhitbox[i])) {
        		if (GameObject.getHitboxType(objid[i]) == 1 ) {
        			//System.out.println((float)objlengthhitbox[i].getHeight() + (float)objlengthhitbox[i].getY());
        			//System.out.println((float)objlengthhitbox[i].getHeight());
        			//System.out.println((float)objlengthhitbox[i].getY());
        			//System.out.println((float)objlengthhitbox[i].getWidth() + (float)objlengthhitbox[i].getX());
        			//System.out.println((float)objlengthhitbox[i].getWidth());
        			//System.out.println((float)objlengthhitbox[i].getX());
        			//System.out.println((float)playerhitbox.getX());
        			//System.out.println((float)playerhitbox.getY());
        			//System.out.println((float)playerhitbox.getHeight());
            		try {
            			if (!isDeath) {
            				death();
            				break;
            			}
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
        		}
        	}
        	if (SharkUtilities.Hitbox.isTouching(smallphitbox, objlengthhitbox[i])  && GameObject.getHitboxType(objid[i]) != 2 && GameObject.getHitboxType(objid[i]) != 3 && GameObject.getHitboxType(objid[i]) != 4) {
        		try {
        			//System.out.println("advanced death log with some positions:");
        			//System.out.println((float)objlengthhitbox[i].getHeight() + (float)objlengthhitbox[i].getY()+ ((float)objlengthhitbox[i].getHeight() * (float)objlengthhitbox[i].getAnchorY()));
        			//System.out.println((float)objlengthhitbox[i].getHeight());
        			//System.out.println((float)objlengthhitbox[i].getX() + ((float)objlengthhitbox[i].getWidth() * (float)objlengthhitbox[i].getAnchorX()));
        			//System.out.println((float)objlengthhitbox[i].getY() + ((float)objlengthhitbox[i].getHeight() * (float)objlengthhitbox[i].getAnchorY()));
        			//System.out.println((float)smallphitbox.getY());
        			//System.out.println((float)smallphitbox.getX());
        			//System.out.println((float)smallphitbox.getHeight());
        			//System.out.println((float)smallphitbox.getWidth());
        			//System.out.println((float)playerhitbox.getX());
        			//System.out.println((float)playerhitbox.getY() + ((float)playerhitbox.getHeight() * (float)playerhitbox.getAnchorY()));
        			//System.out.println((float)playerhitbox.getHeight());
        			if (!isDeath) {
        				death();
        				break;
        			}
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
        	} else {
        		//System.out.println(SharkUtilities.Hitbox.isTouching(smallphitbox, objlengthhitbox[i]));
        	}
    	}
    }
    
    private void checkcolliswithship() {
    	int j = 0;
        for (int i = 0; i < objlength; i++) {
        	Hitbox playerhitboxwithdrop = Hitbox.rect(playerhitbox.getX(), playerhitbox.getY()+ ((float)(GRAVITY * deltaTimeSeconds * 42)/(4-(j/16))), playerhitbox.getWidth(), playerhitbox.getHeight(), playerhitbox.getAnchorX(), playerhitbox.getAnchorY());
        	if (!isFlipped) {
        		playerhitboxwithdrop = Hitbox.rect(playerhitbox.getX(), playerhitbox.getY()+ ((float)(GRAVITY * deltaTimeSeconds * 42)/(4-(j/16))), playerhitbox.getWidth(), playerhitbox.getHeight(), playerhitbox.getAnchorX(), playerhitbox.getAnchorY());
        	} else {
        		playerhitboxwithdrop = Hitbox.rect(playerhitbox.getX(), playerhitbox.getY()- ((float)(GRAVITY * deltaTimeSeconds * 42)/(4-(j/16))), playerhitbox.getWidth(), playerhitbox.getHeight(), playerhitbox.getAnchorX(), playerhitbox.getAnchorY());
        	}
        	boolean secondValue = true;
        	if (SharkUtilities.Hitbox.isTouching(playerhitboxwithdrop, objlengthhitbox[i])
        			&& secondValue
        			&& GameObject.getHitboxType(objid[i]) != 1
        			&& GameObject.getHitboxType(objid[i]) != 2
        			&& GameObject.getHitboxType(objid[i]) != 3 
        			&& GameObject.getHitboxType(objid[i]) != 4) { 
	        	if (!isFlipped) {
			        if (!SharkUtilities.Hitbox.isTouchingU(smallphitbox.expand(-1, -1, 2, 2, 0.0, 0.0), objlengthhitbox[i]) && GameObject.getHitboxType(objid[i]) != 1  && GameObject.getHitboxType(objid[i]) != 2 && GameObject.getHitboxType(objid[i]) != 3 && GameObject.getHitboxType(objid[i]) != 4) {
		        		velocityY = 1.f/8;
		        		playerY = objy[i] + playerhitbox.getHeight() + 1;
		        		isTouched = true;
		        		isGrounded = true;
		        	}
		        	
		        } else {
		        	if (!SharkUtilities.Hitbox.isTouchingD(smallphitbox.expand(-1, -1, 2, 2, 0.0, 0.0), objlengthhitbox[i]) && GameObject.getHitboxType(objid[i]) != 1  && GameObject.getHitboxType(objid[i]) != 2 && GameObject.getHitboxType(objid[i]) != 3 && GameObject.getHitboxType(objid[i]) != 4) {
		        		velocityY = -1.f/8;
		        		playerY = objy[i] - playerhitbox.getHeight() - 1;
		        		isTouched = true;
		        		isGrounded = true;
			        }
		        }
        		
        		//System.out.println(-((-objy[i]) + playerhitbox.getHeight()));
	        	if (!isFlipped) {
	        		if (j < 16 || SharkUtilities.Hitbox.isTouching(playerhitbox, objlengthhitbox[i]) && !SharkUtilities.Hitbox.isTouchingD(smallphitbox, objlengthhitbox[i])) {
	        			//break;
	        		} else {
	        			j++;
	        		}
	        	} else {
	        		if (j < 16 || SharkUtilities.Hitbox.isTouching(playerhitbox, objlengthhitbox[i]) && !SharkUtilities.Hitbox.isTouchingU(smallphitbox, objlengthhitbox[i])) {
	        			//break;
	        		} else {
	        			j++;
	        		}
	        	}
	        	
        	} else {
        		checkjumpaftertouch();
        	}
        }
    }
    
    private void checkcollisions() {
    	int j = 0;
        for (int i = 0; i < objlength; i++) {
        	boolean secondValue = false;
        	Hitbox playerhitboxwithdrop = Hitbox.rect(playerhitbox.getX(), playerhitbox.getY()+ ((float)(GRAVITY * deltaTimeSeconds * 42)/(4-(j/16))), playerhitbox.getWidth(), playerhitbox.getHeight(), playerhitbox.getAnchorX(), playerhitbox.getAnchorY());
        	if (!isFlipped) {
        		playerhitboxwithdrop = Hitbox.rect(playerhitbox.getX(), playerhitbox.getY()+ ((float)(GRAVITY * deltaTimeSeconds * 42)/(4-(j/16))), playerhitbox.getWidth(), playerhitbox.getHeight(), playerhitbox.getAnchorX(), playerhitbox.getAnchorY());
        		secondValue = velocityY > 0;
        	} else {
        		playerhitboxwithdrop = Hitbox.rect(playerhitbox.getX(), playerhitbox.getY()- ((float)(GRAVITY * deltaTimeSeconds * 42)/(4-(j/16))), playerhitbox.getWidth(), playerhitbox.getHeight(), playerhitbox.getAnchorX(), playerhitbox.getAnchorY());
        		secondValue = velocityY < 0;
        	}
        	if (SharkUtilities.Hitbox.isTouching(playerhitbox, objlengthhitbox[i])) {
	        	if (GameObject.getHitboxType(objid[i]) == 2 && !objdataistouching[i]) {
	        		objdataistouching[i] = true;
	        		isTrailing = true;
	        		isGrounded = false;
	        		
	        		touchportals(i);
	        		break;
	        	}
        	}
        	if (SharkUtilities.Hitbox.isTouching(playerhitboxwithdrop, objlengthhitbox[i]) && secondValue) { 
	        	//System.out.println(SharkUtilities.Hitbox.isTouchingV(smallphitbox, objlengthhitbox[i]));
        		//  + (playerhitbox.getHeight() * (float) playerhitbox.getAnchorY())
	        	if (!((playerY) < GROUND_LEVEL-10)) {
	        		if (!isFlipped) {
	        			playerY = (long) (playerY - (playerY - GROUND_LEVEL+10));
	        			velocityY = 1.f/24;
	        			isGrounded = true;
	        		}
	        	}
	        	if (!isFlipped) {
		        	if (!SharkUtilities.Hitbox.isTouchingD(smallphitbox.expand(-1, -1, 2, 2, 0.0, 0.0), objlengthhitbox[i]) && GameObject.getHitboxType(objid[i]) != 1  && GameObject.getHitboxType(objid[i]) != 2 && GameObject.getHitboxType(objid[i]) != 3 && GameObject.getHitboxType(objid[i]) != 4) {
		        			velocityY = 1.f/24;
		        			playerY = objy[i] - playerhitbox.getHeight();
		        		isTouched = true;
		        	}
		        	if (!SharkUtilities.Hitbox.isTouchingD(smallphitbox.expand(-1, -1, 2, 2, 0.0, 0.0), objlengthhitbox[i]) && GameObject.getHitboxType(objid[i]) != 1 && GameObject.getHitboxType(objid[i]) != 2 && GameObject.getHitboxType(objid[i]) != 3 && GameObject.getHitboxType(objid[i]) != 4) {
		        		isGrounded = true;
		        		checkjumpaftertouch();
		        	}
		        	if (type == 1) {
			        	if (!SharkUtilities.Hitbox.isTouchingU(smallphitbox.expand(-1, -1, 2, 2, 0.0, 0.0), objlengthhitbox[i]) && GameObject.getHitboxType(objid[i]) != 1  && GameObject.getHitboxType(objid[i]) != 2 && GameObject.getHitboxType(objid[i]) != 3 && GameObject.getHitboxType(objid[i]) != 4) {
		        			velocityY = -1.f/24;
		        			playerY = objy[i] + playerhitbox.getHeight();
		        		isTouched = true;
		        		isGrounded = true;
			        	}
		        	}
		        	
		        } else {
			        if (!SharkUtilities.Hitbox.isTouchingU(smallphitbox.expand(-1, -1, 2, 2, 0.0, 0.0), objlengthhitbox[i]) && GameObject.getHitboxType(objid[i]) != 1  && GameObject.getHitboxType(objid[i]) != 2 && GameObject.getHitboxType(objid[i]) != 3 && GameObject.getHitboxType(objid[i]) != 4) {
		        		velocityY = -1.f/8;
		        		playerY = objy[i] + playerhitbox.getHeight();
		        		isTouched = true;
		        	}
			        if (!SharkUtilities.Hitbox.isTouchingU(smallphitbox.expand(-1, -1, 2, 2, 0.0, 0.0), objlengthhitbox[i]) && GameObject.getHitboxType(objid[i]) != 1 && GameObject.getHitboxType(objid[i]) != 2 && GameObject.getHitboxType(objid[i]) != 3 && GameObject.getHitboxType(objid[i]) != 4) {
			        	isGrounded = true;
			        	checkjumpaftertouch();
			        }
		        	if (type == 1) {
			        	if (!SharkUtilities.Hitbox.isTouchingD(smallphitbox.expand(-1, -1, 2, 2, 0.0, 0.0), objlengthhitbox[i]) && GameObject.getHitboxType(objid[i]) != 1  && GameObject.getHitboxType(objid[i]) != 2 && GameObject.getHitboxType(objid[i]) != 3 && GameObject.getHitboxType(objid[i]) != 4) {
		        			velocityY = 1.f/24;
		        			playerY = objy[i] - playerhitbox.getHeight();
		        		isTouched = true;
		        		isGrounded = true;
			        	}
		        	}
		        }
	        	if (GameObject.getHitboxType(objid[i]) == 2 && !objdataistouching[i]) {
	        		objdataistouching[i] = true;
	        		touchportals(i);
	        	}
        		
        		//System.out.println(-((-objy[i]) + playerhitbox.getHeight()));
	        	if (!isFlipped) {
	        		if (j < 16 || SharkUtilities.Hitbox.isTouching(playerhitboxwithdrop, objlengthhitbox[i]) && !SharkUtilities.Hitbox.isTouchingD(smallphitbox, objlengthhitbox[i])) {
	        			//break;
	        		} else {
	        			j++;
	        		}
	        	} else {
	        		if (j < 16 || SharkUtilities.Hitbox.isTouching(playerhitboxwithdrop, objlengthhitbox[i]) && !SharkUtilities.Hitbox.isTouchingU(smallphitbox, objlengthhitbox[i])) {
	        			//break;
	        		} else {
	        			j++;
	        		}
	        	}
        	} else {
        		isTouched = false;
        		if (!isFlipped) {
        			if (!SharkUtilities.Hitbox.isTouchingD(smallphitbox.expand(-1, -1, 2, 2, 0.0, 0.0), objlengthhitbox[i]) && GameObject.getHitboxType(objid[i]) != 1  && GameObject.getHitboxType(objid[i]) != 2 && GameObject.getHitboxType(objid[i]) != 3 && GameObject.getHitboxType(objid[i]) != 4) {
        				checkjumpaftertouch();
        			}
        		} else {
        			if (!SharkUtilities.Hitbox.isTouchingU(smallphitbox.expand(-1, -1, 2, 2, 0.0, 0.0), objlengthhitbox[i]) && GameObject.getHitboxType(objid[i]) != 1  && GameObject.getHitboxType(objid[i]) != 2 && GameObject.getHitboxType(objid[i]) != 3 && GameObject.getHitboxType(objid[i]) != 4) {
        				checkjumpaftertouch();
        			}
        		}
        	}
        }
    }
    
    private void checkcollisionswithoutjump() {
    	int j = 0;
        for (int i = 0; i < objlength; i++) {
        	boolean secondValue = false;
        	Hitbox playerhitboxwithdrop = Hitbox.rect(playerhitbox.getX(), playerhitbox.getY()+ ((float)(GRAVITY * deltaTimeSeconds * 42)/(4-(j/16))), playerhitbox.getWidth(), playerhitbox.getHeight(), playerhitbox.getAnchorX(), playerhitbox.getAnchorY());
        	if (!isFlipped) {
        		playerhitboxwithdrop = Hitbox.rect(playerhitbox.getX(), playerhitbox.getY()+ ((float)(GRAVITY * deltaTimeSeconds * 42)/(4-(j/16))), playerhitbox.getWidth(), playerhitbox.getHeight(), playerhitbox.getAnchorX(), playerhitbox.getAnchorY());
        		secondValue = velocityY > 0;
        	} else {
        		playerhitboxwithdrop = Hitbox.rect(playerhitbox.getX(), playerhitbox.getY()- ((float)(GRAVITY * deltaTimeSeconds * 42)/(4-(j/16))), playerhitbox.getWidth(), playerhitbox.getHeight(), playerhitbox.getAnchorX(), playerhitbox.getAnchorY());
        		secondValue = velocityY < 0;
        	}
        	if (SharkUtilities.Hitbox.isTouching(playerhitbox, objlengthhitbox[i])) {
	        	if (GameObject.getHitboxType(objid[i]) == 2 && !objdataistouching[i]) {
	        		objdataistouching[i] = true;
	        		isTrailing = true;
	        		isGrounded = false;
	        		
	        		touchportals(i);
	        		break;
	        	}
        	}
        	if (SharkUtilities.Hitbox.isTouching(playerhitboxwithdrop, objlengthhitbox[i]) && secondValue) {
	        	//System.out.println(SharkUtilities.Hitbox.isTouchingV(smallphitbox, objlengthhitbox[i]));
        		//  + (playerhitbox.getHeight() * (float) playerhitbox.getAnchorY())
	        	if (!((playerY) < GROUND_LEVEL-10)) {
	        		if (!isFlipped) {
	        			playerY = (long) (playerY - (playerY - GROUND_LEVEL+10));
	        			velocityY = 1.f/24;
	        			isGrounded = true;
	        		}
	        	}
	        	
	        	if (!isFlipped) {
	        	if (!SharkUtilities.Hitbox.isTouchingD(smallphitbox.expand(-1, -1, 2, 2, 0.0, 0.0), objlengthhitbox[i]) && GameObject.getHitboxType(objid[i]) != 1  && GameObject.getHitboxType(objid[i]) != 2 && GameObject.getHitboxType(objid[i]) != 3 && GameObject.getHitboxType(objid[i]) != 4) {
	        			velocityY = 1.f/24;
	        			playerY = objy[i] - playerhitbox.getHeight();
	        		isTouched = true;
	        	}} else {
		        	if (!SharkUtilities.Hitbox.isTouchingU(smallphitbox.expand(-1, -1, 2, 2, 0.0, 0.0), objlengthhitbox[i]) && GameObject.getHitboxType(objid[i]) != 1  && GameObject.getHitboxType(objid[i]) != 2 && GameObject.getHitboxType(objid[i]) != 3 && GameObject.getHitboxType(objid[i]) != 4) {
		        		velocityY = -1.f/8;
	        			playerY = objy[i] + playerhitbox.getHeight();
	        		isTouched = true;
	        	}
	        	}
	        	if (GameObject.getHitboxType(objid[i]) == 2 && !objdataistouching[i]) {
	        		objdataistouching[i] = true;
	        		touchportals(i);
	        	}
        		
        		//System.out.println(-((-objy[i]) + playerhitbox.getHeight()));
	        	if (!isFlipped) {
	        		if (j < 16 || SharkUtilities.Hitbox.isTouching(playerhitboxwithdrop, objlengthhitbox[i]) && !SharkUtilities.Hitbox.isTouchingD(smallphitbox, objlengthhitbox[i])) {
	        			//break;
	        		} else {
	        			j++;
	        		}
	        	} else {
	        		if (j < 16 || SharkUtilities.Hitbox.isTouching(playerhitboxwithdrop, objlengthhitbox[i]) && !SharkUtilities.Hitbox.isTouchingU(smallphitbox, objlengthhitbox[i])) {
	        			//break;
	        		} else {
	        			j++;
	        		}
	        	}
        	}
        }
    }
    
    private void touchportals(int i) {
    	border = (int) Math.floor((objlengthhitbox[i].getY()+110)/20);
    	border = Math.min(0, border);
		switch (objid[i]) {
		case 10: {
			isFlipped = false;
			if (-0.5f < velocityY && 0.5f > velocityY) {
				velocityY = 1.f/24;
			} else {
				velocityY /= 2;
			}
			break;
		}
		case 11: {
			isFlipped = true;
			if (-0.5f < velocityY && 0.5f > velocityY) {
				velocityY = -1.f/24;
			} else {
				velocityY /= 2;
			}
			break;
		}
		case 12: {
			type = 0;
			break;
		}
		case 13: {
			type = 1;
			break;
		}
	}
    }
    
    private void death() throws IOException {
    	isDeath = true;
    	try {
    		midiPlayer.stop();
	        InputStream is = getClass().getResourceAsStream("/sounds/midi/explode_11.mid");
	        sfx = Manager.createPlayer(is, "audio/midi");
	        sfx.start();
	        is.close();
	        is = null;
	        draw();
		} catch (MediaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	final Timer aTimer= new Timer();
    	TimerTask ttask = new TimerTask() {
    		public void run() {
    			if (!isPaused && isRunning && isDeath) {
    				restart();
    			} else {
    				isRestartReady = true;
    			}
    		}
    	};
		aTimer.schedule(ttask, 1000);
    }
    
    private void restart() {
		playerX=0;
		playerY=-10;
		cameraY = -159-((getHeight()-240));
		attempts++;
		isFlipped=false;
		type=0;
		progress = 0;
		currentMidiTime = 0;
		for (int i = 0; i < objdataistouching.length; i++) {
			objdataistouching[i] = false;
		}
		try {
			midiPlayer.setMediaTime(0);
			midiPlayer.start();
		} catch (MediaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		isDeath = false;
		isRestartReady = false;
		//aTimer.cancel();
    }
    
    private void playerupdate() {
    	//testing only
    	//Random random = new Random();
    	
    	//int randomNumber = random.nextInt(10) -159;

    	//cameraY = randomNumber;
    	
    	if (!isDeath) {
    	
        progress += 1 * deltaTimeSeconds;
        progressFixed = (int) Math.floor(progress);
        progressTest = deltaTimeMillis;
        
        if (!(velocityY > (0 - JUMP_STRENGTH))) {
        } else {
        	
        }
        checkcollisionswithoutjump();
        checkcollisions();
        switch (type) {
	    	case 0: {
	    		if (type == 0)
	    		GRAVITY = 1.25f;
	    	}
	    	case 1: {
	    		if (type == 1)
	    		GRAVITY = 1.25f /3.5f;
	    	}
	    }
        
        playerX += 10.41667 * deltaTimeSeconds * 20;
        checkcollisionswithoutjump();
        playerY += velocityY * deltaTimeSeconds * 39;
        if (playerY < -1800) {
        	try {
				death();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
        }
        
        if ((playerX > (getWidth() * 0.3f) + 30) && attempts == 1) {
        	cameraX = playerX - (getWidth() * 0.3f);
        } else if (attempts != 1) {
        	cameraX = playerX - (getWidth() * 0.3f);
        } else {
        	cameraX = 30;
        }
        handletrails();
        
        if (!isDeath) {
        
        if (!isTouched) {
	        if (((playerY) < GROUND_LEVEL-10)) {
	        	isGrounded = false;
	        	switch (type) {
		        	case 0: {
		        		if (type == 0)
		        		if (!isFlipped) {
				            if (!(velocityY > 15)) {
				            	velocityY += GRAVITY * deltaTimeSeconds * 42;
				            } else {
				            	velocityY = 15;
				            }
				        	} else {
				            if (!(velocityY < -15)) {
				            	velocityY -= GRAVITY * deltaTimeSeconds * 42;
				            } else {
				            	velocityY = -15;
				            }
			        	}
		        		velocityY = Math.min(15, Math.max(-15, velocityY));
		        	}
		        	case 1: {
		        		if (type == 1)
		        		if (!isFlipped) {
				            if (!(velocityY > 6.4f - GRAVITY * deltaTimeSeconds * 42)) {
				            	velocityY += GRAVITY * deltaTimeSeconds * 42;
				            } else {
				            	velocityY = 6.4f;
				            }
				            if (velocityY < -8f + GRAVITY * deltaTimeSeconds * 42) {
				            	velocityY = -8f;
				            }
				        } else {
				            if (!(velocityY < -6.4f + GRAVITY * deltaTimeSeconds * 42)) {
				            	velocityY -= GRAVITY * deltaTimeSeconds * 42;
				            } else {
				            	velocityY = -6.4f;
				            }
				            if (velocityY > 8f - GRAVITY * deltaTimeSeconds * 42) {
				            	velocityY = 8f;
				            }
			        	}
		        	}
	        	}
	        } else {
        		if (!isFlipped) {
        			playerY = (long) (playerY - (playerY - GROUND_LEVEL+10));
        			velocityY = 1.f/24;
        			isGrounded = true;
        		} else {
	        		if (type == 0)
		        		if (!isFlipped) {
				            if (!(velocityY > 15)) {
				            	velocityY += GRAVITY * deltaTimeSeconds * 42;
				            } else {
				            	velocityY = 15;
				            }
				        	} else {
				            if (!(velocityY < -15)) {
				            	velocityY -= GRAVITY * deltaTimeSeconds * 42;
				            } else {
				            	velocityY = -15;
				            }
			        	}
        		}
	        }
        }
        
		if (!((playerY) < GROUND_LEVEL-0-(border*20))) {
			try {
				death();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        
        if (isTouched) {
        	checkjumpaftertouch();
        }
        
        if (type == 1) {
	        if (!((playerY) < GROUND_LEVEL-10-(border*20))) {
	    		if (!isFlipped) {
	    			playerY = (long) (playerY - (playerY - GROUND_LEVEL+10));
	    			velocityY = 1.f/24;
	    			isGrounded = true;
	    		} else {
	    			playerY = (long) (playerY - (playerY - GROUND_LEVEL+10));
	    			velocityY = -1.f/24;
	    			isGrounded = true;
	    		}
	    		if (!((playerY) < GROUND_LEVEL-0-(border*20))) {
	    			try {
						death();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}
    		}
	        if (!((playerY) > GROUND_LEVEL-210-(border*20))) {
	    		if (isFlipped) {
	    			playerY = (long) (playerY - (playerY - GROUND_LEVEL+210));
	    			velocityY = -1.f/24;
	    			isGrounded = true;
	    		} else {
	    			playerY = (long) (playerY - (playerY - GROUND_LEVEL+210));
	    			velocityY = 1.f/24;
	    			isGrounded = true;
	    		}
	    		if (!((playerY) > GROUND_LEVEL-220-(border*20))) {
	    			try {
						death();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}
    		}
        }
        float LOCAL_GRAVITY = 1.25f/3;
        if (isFlying && !isGrounded) {
        	if (velocityY < 0) {
	        	if (!isFlipped) {
	        		velocityY -= LOCAL_GRAVITY * deltaTimeSeconds * 42 * 2;
	        	} else {
	        		velocityY += LOCAL_GRAVITY * deltaTimeSeconds * 42 * 2;
	        	}
        	} else {
	        	if (!isFlipped) {
	        		velocityY -= GRAVITY * deltaTimeSeconds * 42 * 2.5;
	        	} else {
	        		velocityY += GRAVITY * deltaTimeSeconds * 42 * 2.5;
	        	}
        	}
        	isGrounded = false;
        	isTouched = false;
        }
        if (isFlying && isGrounded) {
        	if (velocityY < 0) {
	        	if (!isFlipped) {
	        		velocityY -= LOCAL_GRAVITY * deltaTimeSeconds * 42 * 2;
	        	} else {
	        		velocityY += LOCAL_GRAVITY * deltaTimeSeconds * 42 * 2;
	        	}
        	} else {
	        	if (!isFlipped) {
	        		velocityY -= GRAVITY * deltaTimeSeconds * 42 * 2.5;
	        	} else {
	        		velocityY += GRAVITY * deltaTimeSeconds * 42 * 2.5;
	        	}
        	}
        	isGrounded = false;
        	isTouched = false;
        }
        
        if (isGrounded && type != 1) {
        	isTrailing = false;
        }
        
        if (type==1) {
        	isTrailing = true;
        }
        
        if (type==0){
        boolean secondValue;
        
        if (!isGrounded) {
        	secondValue = !(velocityY < 1 && velocityY > -1);
        } else {
        	secondValue = true;
        }
        
        if (!isGrounded && secondValue) {
        	if (!isFlipped) {
        		playerAngle += -500 * deltaTimeSeconds;
        	} else {
        		playerAngle += 500 * deltaTimeSeconds;
        	}
        	if (180 < playerAngle) {
        		playerAngle = playerAngle - 360;
        	}
        	
        	if (-180 > playerAngle) {
        		playerAngle = playerAngle + 360;
        	}

        } else {
        	float playerAngletolerp = (float) Math.floor(((playerAngle / 90) + 0.5f))*90;
        	playerAngle = SharkUtilities.lerp(playerAngle, playerAngletolerp, 20f * (float) deltaTimeSeconds);
        	if (-1 < playerAngle && 1 > playerAngle) {
        		playerAngle = 0;
        	}
        	if (88 < playerAngle && 92 > playerAngle) {
        		playerAngle = 90;
        	}
        	if (178 < playerAngle && 182 > playerAngle) {
        		playerAngle = 180;
        	}
        	if (-92 < playerAngle && -88 > playerAngle) {
        		playerAngle = -90;
        	}
        	if (-182 < playerAngle && -178 > playerAngle) {
        		playerAngle = -180;
        	}
        	
        	if (180 < playerAngle) {
        		playerAngle = playerAngle - 360;
        	}
        	
        	if (-180 > playerAngle) {
        		playerAngle = playerAngle + 360;
        	}
        }
        }
        if (type==1) {
        	float playerAngletolerp = (float) SharkUtilities.atan2(velocityY, 10.41667 * deltaTimeSeconds * 40) * -45;
        	playerAngle = SharkUtilities.lerp(playerAngle, playerAngletolerp, 20f * (float) deltaTimeSeconds);
        	if (-1 < playerAngle && 1 > playerAngle) {
        		playerAngle = 0;
        	}
        	if (88 < playerAngle && 92 > playerAngle) {
        		playerAngle = 90;
        	}
        	if (178 < playerAngle && 182 > playerAngle) {
        		playerAngle = 180;
        	}
        	if (-92 < playerAngle && -88 > playerAngle) {
        		playerAngle = -90;
        	}
        	if (-182 < playerAngle && -178 > playerAngle) {
        		playerAngle = -180;
        	}
        	
        	if (180 < playerAngle) {
        		playerAngle = playerAngle - 360;
        	}
        	
        	if (-180 > playerAngle) {
        		playerAngle = playerAngle + 360;
        	}
        	checkcolliswithship();
        }
        
        playerXint = (int)playerX;
        playerYint = (int)playerY;
        GROUND_LEVELint = (int)GROUND_LEVEL;
        smallphitbox = SharkUtilities.Hitbox.rect((int)(playerX+17.5), (int)(playerY+17.5), 5, 5, 0, 0);
        playerhitbox = SharkUtilities.Hitbox.rect(playerX, playerY, playerhitbox.getWidth(), playerhitbox.getHeight(), 0.5, 0.5);
        checkothercollis();
        
        }
        
        float cameraYtolerp = 0;
        
        if ((playerhitbox.getY() + (playerhitbox.getHeight() * (float) playerhitbox.getAnchorY()) - (int)-getHeight()/2)<getHeight()*0.3f) {
        	cameraYtolerp = playerhitbox.getY() - getHeight()*0.3f;
        }
        if ((playerhitbox.getY() + (playerhitbox.getHeight() * (float) playerhitbox.getAnchorY()) - (int)-getHeight()/2)>getHeight()*0.7f) {
        	cameraYtolerp = playerhitbox.getY() - getHeight()*0.7f;
        }
        cameraYtolerp = Math.min(cameraYtolerp, -159-(getHeight()-240));
        if (type==1){
        cameraYtolerp = -212 + (border * 20)-((getHeight()-240)/2);
        }
        cameraY = SharkUtilities.lerp(cameraY, cameraYtolerp, 5f * (float) deltaTimeSeconds);
    }
    
    private void renderground(Graphics g, float y, boolean isUpsideDown) {
    	if (!isUpsideDown) {
        	for (int i = 0; i-75 < 320; i += 75) {
            	SharkUtilities.drawImageWithAnchor(gnimage, (int) -cameraX+i+(int) (Math.floor((double) (cameraX/75))*75), (int) y, 0, 0.0, 0.0, g);
            }
            
            SharkUtilities.drawImageWithAnchor(gnshadow, 0, (int) y, 0, 0, 0, g);
            SharkUtilities.drawImageWithAnchor(SharkUtilities.flipImageHorizontal(gnshadow), getWidth(), (int) y, 0, -1, 0, g);
            
            SharkUtilities.drawImageWithAnchor(lineimage, getWidth() / 2, (int) y, 0, -0.5, 0, g);
    	} else {
        	for (int i = 0; i-75 < 320; i += 75) {
            	SharkUtilities.drawImageWithAnchor(SharkUtilities.flipImageVertical(gnimage), (int) -cameraX+i+(int) (Math.floor((double) (cameraX/75))*75), (int) y-75, 0, 0.0, 0.0, g);
            }
            
            SharkUtilities.drawImageWithAnchor(gnshadow, 0, (int) y-75, 0, 0, 0, g);
            SharkUtilities.drawImageWithAnchor(SharkUtilities.flipImageHorizontal(gnshadow), getWidth(), (int) y-75, 0, -1, 0, g);
            
            SharkUtilities.drawImageWithAnchor(lineimage, getWidth() / 2, (int) y-lineimage.getHeight(), 0, -0.5, 0, g);
    	}
    }

    private void draw() throws IOException {
        Graphics g = getGraphics();
        
        g.setColor(0);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        SharkUtilities.drawImageWithAnchor(bgimage, (int) (-cameraX * 0.1f) + (int) (Math.floor((double) ((cameraX * 0.1f)/400))*400), (int) (-getHeight()+GROUND_LEVEL+80+(int) ((-cameraY - 159) * 0.1f))+(int)((getHeight()-240)*1.5f), 0, 0, 0, g);
        SharkUtilities.drawImageWithAnchor(bgimage, (int) (-cameraX * 0.1f) + 400 + (int) (Math.floor((double) ((cameraX * 0.1f)/400))*400), (int) (-getHeight()+GROUND_LEVEL+80+(int) ((-cameraY - 159) * 0.1f))+(int)((getHeight()-240)*1.5f), 0, 0, 0, g);
        
        g.setColor(0xFFFFFF);
        g.drawLine(0, (int) GROUND_LEVELint + 20 - (int)cameraY, getWidth(), (int) GROUND_LEVELint + 20 - (int)cameraY);
        //g.drawLine(0, (int) GROUND_LEVELint + 0 - (int)cameraY, getWidth(), (int) GROUND_LEVELint + 0 - (int)cameraY);
        //g.drawLine(0, (int) GROUND_LEVELint + -20 - (int)cameraY, getWidth(), (int) GROUND_LEVELint + -20 - (int)cameraY);
        //g.drawLine(0, (int) GROUND_LEVELint + -40 - (int)cameraY, getWidth(), (int) GROUND_LEVELint + -40 - (int)cameraY);
        
        g.setColor(0xFFFF00);
        
        //SharkUtilities.drawHitbox(playerhitbox.getX() + (playerhitbox.getWidth() * (float) playerhitbox.getAnchorX()) - (int)cameraX, playerhitbox.getY() + (playerhitbox.getHeight() * (float) playerhitbox.getAnchorY()) - (int)cameraY, playerhitbox.getWidth(), playerhitbox.getHeight(), g, 0x0000ff, 0);
        //SharkUtilities.drawHitbox(smallphitbox.getX() + (smallphitbox.getWidth() * (float) smallphitbox.getAnchorX()) - (int)cameraX, smallphitbox.getY() + (smallphitbox.getHeight() * (float) smallphitbox.getAnchorY()) - (int)cameraY, smallphitbox.getWidth(), smallphitbox.getHeight(), g, 0xff0000, 0);
        //g.drawRect((int)(playerhitbox.getX()+(playerhitbox.getWidth() * (float) playerhitbox.getAnchorX())-(int)cameraX), (int)(playerhitbox.getY()+(playerhitbox.getHeight() * (float) playerhitbox.getAnchorY())-(int)cameraY), 0, 0);
        //g.drawRect((int)(playerhitbox.getX()+playerhitbox.getWidth()+(playerhitbox.getWidth() * (float) playerhitbox.getAnchorX())-(int)cameraX), (int)(playerhitbox.getY()+playerhitbox.getHeight()+(playerhitbox.getHeight() * (float) playerhitbox.getAnchorY())-(int)cameraY), 0, 0);
        //SharkUtilities.drawHitboxWithRect(playerhitbox, g, 0x0000ff, 0);
        
        //
        TextUtilities.setFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE, g);
        
        for (int i = 0; i < objlength; i++) {
        	if (objx[i]-cameraX<getWidth()+20&&objx[i]-cameraX>-40) {
        	GameObject.render(objid[i], objx[i]-cameraX, objy[i]-cameraY, 0.5, 0.5, objimage, g);
        	//if (GameObject.getHitboxType(objid[i]) == 1) {
        	//	SharkUtilities.drawHitboxWithRect(Hitbox.rect(objlengthhitbox[i].getX()-cameraX, objlengthhitbox[i].getY()-cameraY, objlengthhitbox[i].getWidth(), objlengthhitbox[i].getHeight(), objlengthhitbox[i].getAnchorX(), objlengthhitbox[i].getAnchorY()), g, 0xff0000, 0);
        	//} else {
        	//	SharkUtilities.drawHitboxWithRect(Hitbox.rect(objlengthhitbox[i].getX()-cameraX, objlengthhitbox[i].getY()-cameraY, objlengthhitbox[i].getWidth(), objlengthhitbox[i].getHeight(), objlengthhitbox[i].getAnchorX(), objlengthhitbox[i].getAnchorY()), g, 0x0000ff, 0);
        	//}
        	//g.setColor(0xff80ff);s
        	//g.drawRect((int)((float)objlengthhitbox[i].getWidth() + (float)objlengthhitbox[i].getX()+ ((float)objlengthhitbox[i].getWidth() * (float)objlengthhitbox[i].getAnchorX()))-(int)cameraX, (int)((float)objlengthhitbox[i].getHeight() + (float)objlengthhitbox[i].getY()+ ((float)objlengthhitbox[i].getHeight() * (float)objlengthhitbox[i].getAnchorY()))-(int)cameraY, 0, 0);
        	//g.drawRect((int)((float)objlengthhitbox[i].getX()+ ((float)objlengthhitbox[i].getWidth() * (float)objlengthhitbox[i].getAnchorX()))-(int)cameraX, (int)((float)objlengthhitbox[i].getY()+ ((float)objlengthhitbox[i].getHeight() * (float)objlengthhitbox[i].getAnchorY()))-(int)cameraY, 0, 0);
        	}
        }
        if (isDone) {
        	rendertrails(g);
        	PlayerObject.renderPlayer(player_2, player, ship, playerAngle, playerXint, cameraX, playerYint, cameraY, type, isFlipped, g);
        }
        
        for (int i = 0; i-75 < 320; i += 75) {
        	SharkUtilities.drawImageWithAnchor(gnimage, (int) -cameraX+i+(int) (Math.floor((double) (cameraX/75))*75), (int) -cameraY+20, 0, 0.0, 0.0, g);
        }
        
        SharkUtilities.drawImageWithAnchor(gnshadow, 0, (int) -cameraY+20, 0, 0, 0, g);
        SharkUtilities.drawImageWithAnchor(SharkUtilities.flipImageHorizontal(gnshadow), getWidth(), (int) -cameraY+20, 0, -1, 0, g);
        
        SharkUtilities.drawImageWithAnchor(lineimage, getWidth() / 2, (int) -cameraY+20, 0, -0.5, 0, g);
        if (type==1) {
        	renderground(g, 230+((getHeight()-240)/2), false);
        	renderground(g, 10+((getHeight()-240)/2), true);
        }
        //
        try {
        	CustomFont.drawString(bigFontbig, (int)(((getWidth() / 2)+((0-(int)cameraX)+30-(("Attempt "+attempts).length() * 22*0.6f*0.5f)))), getHeight() / 3, .6f, ("Attempt "+attempts), 22, g);
        } catch (Exception e) {
        	System.err.println("Details: " + e.getMessage());
        }
        
        g.drawImage(pauseBtn, getWidth() - 24, 4, 0);
        
        String progressFixedStr = Integer.toString(progressFixed) + "%";
        
        g.setColor(0xFFFFFF);
        TextUtilities.setFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE, g);
        //TextUtilities.drawOutlinedString(progressFixed + "%", (getWidth() / 2) - (progressFixedStr.length() * 5), 0, 0, g, 0xFFFFFF, 0x000000);
        CustomFont.drawString(bigFontbig, (getWidth() / 2) - (int)(progressFixedStr.length() * 22 * 0.25f), 0, 0.5f, progressFixed + "%", 22, g);
        
        
        String debugStr = "gameVerID:"+gameVerID+"\nlevelName:"+levelName+"\nbgColor:"+bgColor+"\ngnColor:"+gnColor;
        String debugStr2 = "\npyvel:"+velocityY+"\ncx:"+cameraX+"\ndtms:"+deltaTimeMillis+"\nkeystate:"+getKeyStates()+"\nobjlength:"+objx[1];
        
        TextUtilities.setFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL, g);
        //TextUtilities.drawOutlinedString(debugStr, 0, 0, 0, g, 0xFFFFFF, 0x000000);
        //TextUtilities.drawOutlinedString(debugStr2, 0, 12, 0, g, 0xFFFFFF, 0x000000);
        TextUtilities.drawOutlinedString("" + playerY, 0, 24, 0, g, 0xFFFFFF, 0x000000);
        
        if (isPaused) {
        	//
        	TextUtilities.setFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE, g);
        	g.drawImage(pausemenubg, 0, 0, 0);
        	TextUtilities.drawOutlinedString(levelName, 0, 0, 0, g, 0xffffff, 0x000000);
        }
        
        //SharkUtilities.splitImg(player, 0, 0, 10, 5);
        
        flushGraphics();
    }
    
    private void drawloading() {
    	Graphics g = getGraphics();
    	g.setColor(0x000000);
    	g.fillRect(0, 0, getWidth(), getHeight());
    	CustomFont.drawString(bigFont, getWidth() - ("Loading...".length() * 11), getHeight() - 17, 1f, "Loading...", 11, g);
    	flushGraphics();
    }
    
    class Point {
        public int x, y;
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}