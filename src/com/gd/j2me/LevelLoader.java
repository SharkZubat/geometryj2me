package com.gd.j2me;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

import com.tsg.hitbox.Direction;

import java.io.IOException;
import java.util.*;

public class LevelLoader {
	public static void Load(String data) throws IOException {
		Launcher.levelGame.objsize = 1;
		Launcher.levelGame.addobj(new GameObject(1,525,45,false,false,new Direction(0)));
	}
}
