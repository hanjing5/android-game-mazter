package com.pickle.mazzter;

public interface MazzterConstants {
	//Game Settings
	public static int CAMERA_WIDTH = 720;
	public static int CAMERA_HEIGHT = 480;
	public static int BACKGROUND_TILE_SIZE = 128;
	
	public static int LEVEL_COUNT = 3;
	
	//Menu Definitons
	public static final int MENU_RESET = 0;
	public static final int MENU_OK = MENU_RESET + 1;
	public static final int MENU_NEXT_LEVEL = MENU_OK + 1;
	public static final int MENU_SKIP = MENU_NEXT_LEVEL + 1;
	public static final int MENU_SCOREBOARD = MENU_SKIP +1;
	
	//Xml Reading Definition
	public static final String TAG_ENTITY = "entity";
	public static final String TAG_ENTITY_ATTRIBUTE_X = "x";
	public static final String TAG_ENTITY_ATTRIBUTE_Y = "y";
	public static final String TAG_ENTITY_ATTRIBUTE_WIDTH = "width";
	public static final String TAG_ENTITY_ATTRIBUTE_HEIGHT = "height";
	public static final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";
}
