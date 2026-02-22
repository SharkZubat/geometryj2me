package com.gd.j2me;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

import com.tsg.hitbox.Direction;

import java.io.IOException;
import java.util.*;

public class LevelLoader {
	
	/**
	 * Load an level from a pre decompressed object data file
	 */
	public static void LoadDecompressed(String filename) throws IOException {
		System.out.println("Loading decompressed level: " + filename);
		
		// Parse the decompressed file
		GameObject[] objects = GMDParser.parseDecompressed(filename);
		
		System.out.println("Loaded " + objects.length + " objects");
		
		// Set the object count
		Launcher.levelGame.objsize = objects.length;
		
		// Add all objects to the level
		for (int i = 0; i < objects.length; i++) {
			Launcher.levelGame.addobj(objects[i]);
		}
		
		System.out.println("Level loaded successfully!");
	}
	
	/**
	 * Load a level from a GMD file
	 * @param filename - path to GMD file (e.g. "/levels/StereoMadness.gmd")
	 */
	public static void Load(String filename) throws IOException {
		System.out.println("Loading level: " + filename);
		
		// Parse the GMD file
		GameObject[] objects = GMDParser.parseGMD(filename);
		
		System.out.println("Loaded " + objects.length + " objects from GMD");
		
		// Set the object count
		Launcher.levelGame.objsize = objects.length;
		
		// Add all objects to the level
		for (int i = 0; i < objects.length; i++) {
			Launcher.levelGame.addobj(objects[i]);
		}
		
		System.out.println("Level loaded successfully!");
	}
	
	/**
	 * Legacy load method - loads a test level
	 */
	public static void LoadTest() throws IOException {
		Launcher.levelGame.objsize = 10;
		
		// Create some test objects
		for (int i = 0; i < 10; i++) {
			Launcher.levelGame.addobj(
				new GameObject(1, 30 * i, 45, false, false, new Direction(0))
			);
		}
	}
}