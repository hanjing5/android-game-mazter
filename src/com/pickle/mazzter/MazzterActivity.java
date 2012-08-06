package com.pickle.mazzter;

import java.io.IOException;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.audio.music.Music;
import org.anddev.andengine.audio.music.MusicFactory;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.camera.SmoothCamera;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.WakeLockOptions;
import org.anddev.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.scene.menu.MenuScene;
import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
import org.anddev.andengine.entity.scene.menu.item.TextMenuItem;
import org.anddev.andengine.entity.scene.menu.item.decorator.ColorMenuItemDecorator;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.input.touch.detector.ClickDetector;
import org.anddev.andengine.input.touch.detector.SurfaceScrollDetector;
import org.anddev.andengine.level.LevelLoader;
import org.anddev.andengine.level.LevelLoader.IEntityLoader;
import org.anddev.andengine.level.util.constants.LevelConstants;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
import org.anddev.andengine.util.Debug;
import org.anddev.andengine.util.HorizontalAlign;
import org.anddev.andengine.util.SAXUtils;
import org.xml.sax.Attributes;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.scoreloop.client.android.ui.EntryScreenActivity;
import com.scoreloop.client.android.ui.ScoreloopManager;
import com.scoreloop.client.android.ui.ScoreloopManagerSingleton;

public class MazzterActivity extends MazzterBase implements IAccelerometerListener, IOnSceneTouchListener, IOnMenuItemClickListener {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	private Music mMusic;
	private Font mFont;

	private Sprite home;

	private Sprite ballSprite;
	private Body ballBody;
	private PhysicsConnector ballPhysicsConnector;
	private FixtureDef objectFixtureDef;

	private TextureRegion mBallTextureRegion;
	private TextureRegion mPipeVerticalTextureRegion;
	private TextureRegion mPipeHorizontalTextureRegion;
	private TextureRegion mPipeCornerTopLeftRegion;
	private TextureRegion mPipeCornerTopRightRegion;
	private TextureRegion mPipeCornerBottomRightRegion;
	private TextureRegion mPipeCornerBottomLeftRegion;
	private TextureRegion mBackgroundTextureRegion;
	private TextureRegion mHomeTextureRegion;
	private TextureRegion mBadTextureRegion;
	private TextureRegion mLevelSelectRegion;

	private BitmapTextureAtlas mFontTexture;
	private BitmapTextureAtlas mPipeTextureAtlas;
	private BitmapTextureAtlas mHomeTextureAtlas;
	private BitmapTextureAtlas mBallTextureAtlas;
	private BitmapTextureAtlas mBackgroundTextureAtlas;
	private BitmapTextureAtlas mLevelSelectorTextureAtlas;

	private ArrayList<Shape> spriteList = new ArrayList<Shape>();
	private ArrayList<Body> bodyList = new ArrayList<Body>();
	private ArrayList<PhysicsConnector> physconnectList = new ArrayList<PhysicsConnector>();
	private ArrayList<Joint> jointList = new ArrayList<Joint>();

	private TimerHandler vibrateTimerHandler;
	// ===========================================================
	// Methods
	// ===========================================================

	@Override
	public void onLoadResources() {

		// Paths
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		MusicFactory.setAssetBasePath("mfx/");
		FontFactory.setAssetBasePath("font/");

		LoadTextures();
		
		// Font
		LoadFontTexture();
	}

	@Override
	public Engine onLoadEngine() {

		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new FillResolutionPolicy(), this.mCamera);

		engineOptions.setNeedsMusic(true);
		engineOptions.getTouchOptions().setRunOnUpdateThread(true);

		final Engine engine = new Engine(engineOptions);
		engine.getEngineOptions().setWakeLockOptions(WakeLockOptions.SCREEN_BRIGHT);

		engine.enableVibrator(this);
		
		return engine;
	}

	@Override
	public Scene onLoadScene() {

		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();

		this.mScene.setBackground(new ColorBackground(1, 1, 1));

		this.mScene.setOnSceneTouchListener(this);

		this.mMenuScene = createMenuScene();

		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);

		mCurrentLevel = 1;

		LoadMaze(this.mScene);

		// Set World
		this.mScene.registerUpdateHandler(this.mPhysicsWorld);

		// Create Level Select Scene
		isLevelSelecting = true;
		
    	return CreateLevelBoxes();

	}

	private void LoadBall() {

		if (ballPhysicsConnector != null) {

			mPhysicsWorld.unregisterPhysicsConnector(ballPhysicsConnector);
			mPhysicsWorld.destroyBody(ballBody);
			mScene.detachChild(ballSprite);

		}
		// Ball
		objectFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.1f);
		ballSprite = new Sprite(20, 20, this.mBallTextureRegion);
		// ballSprite.setScale(0.25f);

		ballBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld, ballSprite, BodyType.DynamicBody, objectFixtureDef);
		this.mScene.attachChild(ballSprite);

		ballPhysicsConnector = new PhysicsConnector(ballSprite, ballBody, true, true);
		this.mPhysicsWorld.registerPhysicsConnector(ballPhysicsConnector);

	}
	
	
	private void LoadBackground(Scene scene) {
		for (int x = 0; x < CAMERA_WIDTH; x += BACKGROUND_TILE_SIZE) {
			for (int y = 0; y < CAMERA_HEIGHT; y += BACKGROUND_TILE_SIZE) {
				Sprite mBackground = new Sprite(x, y, BACKGROUND_TILE_SIZE, BACKGROUND_TILE_SIZE, this.mBackgroundTextureRegion);
				this.mScene.attachChild(mBackground);
				spriteList.add(mBackground);
			}
		}
	}

	private void addRingTrap(final Scene pScene, int rings, int x, int y) {

		final int centerX = x;
		final int centerY = y;

		// zero based
		rings++;

		final int spriteWidth = this.mBallTextureRegion.getWidth();
		final int spriteHeight = this.mBallTextureRegion.getWidth();

		final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(10, 0.0f, 0.0f);

		final float anchorFaceX = centerX - (spriteWidth / 2);// * 0.5f + 220 *
																// (i - 1);
		final float anchorFaceY = centerY + (spriteHeight / 2);// * 0.5f;

		final Sprite anchorFace = new Sprite(anchorFaceX, anchorFaceY, this.mBallTextureRegion);
		final Body anchorBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld, anchorFace, BodyType.KinematicBody, objectFixtureDef);

		PhysicsConnector physAnchorConnector = new PhysicsConnector(anchorFace, anchorBody, true, true);
		this.mPhysicsWorld.registerPhysicsConnector(physAnchorConnector);
		spriteList.add(anchorFace);
		bodyList.add(anchorBody);
		physconnectList.add(physAnchorConnector);

		for (int i = 1; i < rings; i++) {
			final Sprite movingFace = new Sprite(anchorFaceX, anchorFaceY + 40 * i, this.mBallTextureRegion);
			final Body movingBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld, movingFace, BodyType.DynamicBody, objectFixtureDef);


			Ellipse elipse = new Ellipse(anchorFaceX + 8, anchorFaceY + 8, 40 * i);
			elipse.setAlpha(0.3f);
			pScene.attachChild(elipse);
			// connectionLine.setAlpha(0.0f);

			// pScene.attachChild(connectionLine);

			pScene.attachChild(movingFace);

			PhysicsConnector physConnector = new PhysicsConnector(movingFace, movingBody, true, true);
			this.mPhysicsWorld.registerPhysicsConnector(physConnector);

			final RevoluteJointDef revoluteJointDef = new RevoluteJointDef();
			revoluteJointDef.initialize(anchorBody, movingBody, anchorBody.getWorldCenter());
			revoluteJointDef.enableMotor = true;
			revoluteJointDef.motorSpeed = 7 - i;
			revoluteJointDef.maxMotorTorque = 100;

			Joint joint = this.mPhysicsWorld.createJoint(revoluteJointDef);

			spriteList.add(elipse);
			spriteList.add(movingFace);
			bodyList.add(movingBody);
			physconnectList.add(physConnector);
			jointList.add(joint);
		}
		anchorFace.setVisible(false);
		pScene.attachChild(anchorFace);

	}

	

	private void LoadMaze(final Scene scene) {

		for (PhysicsConnector physConnector : physconnectList) {
			this.mPhysicsWorld.unregisterPhysicsConnector(physConnector);
		}
		physconnectList.clear();

		for (Shape sprite : spriteList) {
			scene.detachChild(sprite);
		}
		spriteList.clear();

		for (Body body : bodyList) {
			this.mPhysicsWorld.destroyBody(body);
		}

		this.mPhysicsWorld.clearForces();
		this.mPhysicsWorld.clearPhysicsConnectors();
		bodyList.clear();

		String levelName = "level" + mCurrentLevel + ".lvl";

		LoadBackground(this.mScene);

		final LevelLoader levelLoader = new LevelLoader();
		levelLoader.setAssetBasePath("level/");

		levelLoader.registerEntityLoader(LevelConstants.TAG_LEVEL, new IEntityLoader() {
			@Override
			public void onLoadEntity(final String pEntityName, final Attributes pAttributes) {

			}
		});

		levelLoader.registerEntityLoader(TAG_ENTITY, new IEntityLoader() {
			@Override
			public void onLoadEntity(final String pEntityName, final Attributes pAttributes) {
				final int x = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_X);
				final int y = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_Y);
				final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_WIDTH);
				final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_HEIGHT);
				final String type = SAXUtils.getAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_TYPE);

				MazzterActivity.this.addLevelItem(mScene, x, y, width, height, type, pAttributes);
			}
		});

		try {
			levelLoader.loadLevelFromAsset(this, levelName);
		} catch (final IOException e) {
			Debug.e(e);
		}

		LoadBall();

	}

	private void addLevelItem(final Scene pScene, final float pX, final float pY, final int pWidth, final int pHeight, final String pType, Attributes pAttributes) {

		int x = (int) pX;
		int y = (int) pY;

		final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(10, 0.5f, 0.5f);
		Sprite pipe = null;

		if (pType.compareTo("pipehorizontal") == 0) {
			pipe = new Sprite(x, y, this.mPipeHorizontalTextureRegion);
		} else if (pType.compareTo("pipevertical") == 0) {
			pipe = new Sprite(pX, pY, this.mPipeVerticalTextureRegion);
		} else if (pType.compareTo("pipetopright") == 0) {
			pipe = new Sprite(pX, pY, this.mPipeCornerTopRightRegion);
		} else if (pType.compareTo("pipetopleft") == 0) {
			pipe = new Sprite(pX, pY, this.mPipeCornerTopLeftRegion);
		} else if (pType.compareTo("pipebottomright") == 0) {
			pipe = new Sprite(pX, pY, this.mPipeCornerBottomRightRegion);
		} else if (pType.compareTo("pipebottomleft") == 0) {
			pipe = new Sprite(pX, pY, this.mPipeCornerBottomLeftRegion);
		}
		
	
		if (pType.compareTo("home") == 0) {
			pipe = new Sprite(pX, pY, this.mHomeTextureRegion) {
				@Override
				protected void onManagedUpdate(float pSecondsElapsed) {
					if (ballSprite.collidesWith(this)) {
						if (collides(this, ballSprite, 12, 12)) {
							setAdVisibility(true);
							mEngine.vibrate(200);
							mMenuScene.clearChildScene();
							mMenuScene = createNextLevelMenuScene();

							mScene.detachChild(home);
							mScene.setChildScene(mMenuScene, false, true, true);

							// Award level complete!
							MazzterActivity.this.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									try {
										final ScoreloopManager manager = ScoreloopManagerSingleton.get();
										if (manager.hasLoadedAchievements()) {
											String awardString = "com.pickle.mazzter.level" + mCurrentLevel;
											ScoreloopManagerSingleton.get().achieveAward(awardString, true, true);
										}
									} catch (Exception e) {
									}
								}
							});

							LoadBall();
						}
					}
					super.onManagedUpdate(pSecondsElapsed);
				}
			};
		} else if (pType.compareTo("bad") == 0) {
			pipe = new Sprite(pX, pY, this.mBadTextureRegion) {
				@Override
				protected void onManagedUpdate(float pSecondsElapsed) {
					if (this.collidesWith(ballSprite)) {

						if (collides(this, ballSprite, 12, 12)) {
							setAdVisibility(true);
							mEngine.vibrate(200);
							mMenuScene.clearChildScene();
							mMenuScene = createRetryLevelMenuScene();

							mScene.detachChild(home);
							mScene.setChildScene(mMenuScene, false, true, true);

							LoadBall();
						}

					}
					super.onManagedUpdate(pSecondsElapsed);
				}
			};
		} else if (pType.compareTo("ringtrap") == 0) {
			int rings = SAXUtils.getIntAttributeOrThrow(pAttributes, "rings");
			addRingTrap(this.mScene, rings, x, y);
		}

		if (pipe != null) {
			// pipe.setScale(0.5f);
			Body body = null;
			if ((pType.compareTo("bad") == 0)) {
				body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, pipe, BodyType.KinematicBody, objectFixtureDef);
			} else if (pType.compareTo("home") == 0) {
				body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, pipe, BodyType.KinematicBody, objectFixtureDef);
			} else {
				body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, pipe, BodyType.StaticBody, objectFixtureDef);
			}

			pScene.attachChild(pipe);

			PhysicsConnector physConnector = new PhysicsConnector(pipe, body, true, true);
			this.mPhysicsWorld.registerPhysicsConnector(physConnector);

			spriteList.add(pipe);
			bodyList.add(body);
			physconnectList.add(physConnector);
		}
	}

	@Override
	public boolean onMenuItemClicked(final MenuScene pMenuScene, final IMenuItem pMenuItem, final float pMenuItemLocalX, final float pMenuItemLocalY) {

		switch (pMenuItem.getID()) {

		case MENU_RESET:
			return true;
		case MENU_OK:

			setAdVisibility(false);
			this.mMenuScene.back();
			return true;

		case MENU_NEXT_LEVEL:
			setAdVisibility(false);
			if (mCurrentLevel == LEVEL_COUNT) {
				mCurrentLevel = 1;
			} else {
				mCurrentLevel++;
			}
			LoadMaze(this.mScene);
			this.mMenuScene.back();
			return true;

		case MENU_SKIP:
			mEngine.setScene(mLevelSelectScene);
			isLevelSelecting = true;
			this.mMenuScene.back();
			return true;
		case MENU_SCOREBOARD:
			startActivity(new Intent(MazzterActivity.this, EntryScreenActivity.class));
			return true;

		default:
			return false;
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================

	public boolean collides(Sprite sprite1, Sprite sprite2, float radius1, float radius2) {

		float x1 = sprite1.getX() + sprite1.getWidth() / 2;// if find the
															// coordinates of
		float y1 = sprite1.getY() + sprite1.getHeight() / 2;
		float x2 = sprite2.getX() + sprite2.getWidth() / 2;
		float y2 = sprite2.getY() + sprite2.getHeight() / 2;

		double dist = Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2)); // i
																					// find
																					// the
																					// distance
		return ((dist < (radius1 + radius2)) ? true : false);// result
	}

	protected void LoadFontTexture() {
		this.mFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mFont = FontFactory.createFromAsset(this.mFontTexture, this, "tahoma.ttf", 48, true, Color.WHITE);
		this.mEngine.getTextureManager().loadTexture(this.mFontTexture);
		this.mEngine.getFontManager().loadFont(this.mFont);
	}

	protected void LoadTextures() {

		// Ball
		this.mBallTextureAtlas = new BitmapTextureAtlas(16, 16, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mBallTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBallTextureAtlas, this, "ball.png", 0, 0);

		// Background
		this.mBackgroundTextureAtlas = new BitmapTextureAtlas(BACKGROUND_TILE_SIZE, BACKGROUND_TILE_SIZE);
		this.mBackgroundTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBackgroundTextureAtlas, this, "backgrounds/parquet128.png", 0, 0);

		// Pipes
		this.mPipeTextureAtlas = new BitmapTextureAtlas(128, 32);
		this.mPipeHorizontalTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mPipeTextureAtlas, this, "pipe-horizontal-16.png", 0, 0);
		this.mPipeVerticalTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mPipeTextureAtlas, this, "pipe-vertical-16.png", 16, 0);
		this.mPipeCornerBottomLeftRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mPipeTextureAtlas, this, "pipe-corner-bottom-left-16.png", 32, 0);
		this.mPipeCornerBottomRightRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mPipeTextureAtlas, this, "pipe-corner-bottom-right-16.png", 48, 0);
		this.mPipeCornerTopLeftRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mPipeTextureAtlas, this, "pipe-corner-top-left-16.png", 64, 0);
		this.mPipeCornerTopRightRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mPipeTextureAtlas, this, "pipe-corner-top-right-16.png", 80, 0);
		// Home
		this.mHomeTextureAtlas = new BitmapTextureAtlas(64, 32);
		this.mHomeTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mHomeTextureAtlas, this, "home.png", 0, 0);
		this.mBadTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mHomeTextureAtlas, this, "bad.png", 32, 0);

		// Level Selector
		this.mLevelSelectorTextureAtlas = new BitmapTextureAtlas(64, 64);
		this.mLevelSelectRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mLevelSelectorTextureAtlas, this, "levelcircle.png", 0, 0);

		// Load Textures
		this.mEngine.getTextureManager().loadTextures(mBallTextureAtlas, mBackgroundTextureAtlas, mPipeTextureAtlas, mHomeTextureAtlas, mLevelSelectorTextureAtlas);

	}
	 
	// ===========================================================
	// Menus
	// ==========================================================

	protected MenuScene createRetryLevelMenuScene() {

		LoadFontTexture();

		MenuScene menuScene = new MenuScene(this.mCamera);

		Rectangle rect = new Rectangle(20, 20, CAMERA_WIDTH - 40, CAMERA_HEIGHT - 350);
		rect.setColor(0, 0, 0);
		rect.setAlpha(0.8f);

		final Text textCenter = new Text(180, 20, this.mFont, "Oops! Try again?", HorizontalAlign.LEFT);

		final IMenuItem quitMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_OK, this.mFont, "Ok!"), 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
		quitMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(quitMenuItem);

		final IMenuItem nextLevelMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_SKIP, this.mFont, "Level Select"), 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
		nextLevelMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(nextLevelMenuItem);

		final IMenuItem scoreBoardMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_SCOREBOARD, this.mFont, "Score Board"), 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
		scoreBoardMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(scoreBoardMenuItem);

		menuScene.buildAnimations();

		menuScene.setBackgroundEnabled(false);

		menuScene.setOnMenuItemClickListener(this);

		menuScene.attachChild(rect);
		menuScene.attachChild(textCenter);

		return menuScene;
	}

	protected MenuScene createNextLevelMenuScene() {
		LoadFontTexture();

		MenuScene menuScene = new MenuScene(this.mCamera);

		Rectangle rect = new Rectangle(20, 20, CAMERA_WIDTH - 40, CAMERA_HEIGHT - 350);
		rect.setColor(0, 0, 0);
		rect.setAlpha(0.5f);

		final Text textCenter = new Text(200, 20, this.mFont, "Congratulations\n You made it!", HorizontalAlign.LEFT);

		final IMenuItem quitMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_NEXT_LEVEL, mFont, "Next Level"), 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
		menuScene.addMenuItem(quitMenuItem);

		final IMenuItem scoreBoardMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_SCOREBOARD, this.mFont, "Score Board"), 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
		scoreBoardMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(scoreBoardMenuItem);

		menuScene.buildAnimations();

		menuScene.setBackgroundEnabled(false);

		menuScene.setOnMenuItemClickListener(this);

		menuScene.attachChild(rect);
		menuScene.attachChild(textCenter);

		return menuScene;
	}

	protected MenuScene createMenuScene() {
		LoadFontTexture();

		MenuScene menuScene = new MenuScene(this.mCamera);

		Rectangle rect = new Rectangle(20, 20, CAMERA_WIDTH - 40, CAMERA_HEIGHT - 350);
		rect.setColor(0, 0, 0);
		rect.setAlpha(0.5f);

		final Text textCenter = new Text(200, 20, this.mFont, "Get the ball to\nthe green hole!", HorizontalAlign.CENTER);

		final IMenuItem okMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_OK, this.mFont, "Ok!"), 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
		okMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(okMenuItem);

		final IMenuItem nextLevelMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_SKIP, this.mFont, "Level Select"), 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
		nextLevelMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(nextLevelMenuItem);

		final IMenuItem scoreBoardMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_SCOREBOARD, this.mFont, "Score Board"), 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
		scoreBoardMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(scoreBoardMenuItem);

		menuScene.buildAnimations();

		menuScene.setBackgroundEnabled(false);

		menuScene.setOnMenuItemClickListener(this);

		menuScene.attachChild(rect);
		menuScene.attachChild(textCenter);

		return menuScene;
	}

	private Scene CreateLevelBoxes() {

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(MazzterActivity.this, "Select a level to start!", Toast.LENGTH_SHORT).show();
			}
		});
		
		LoadFontTexture();
		
		this.mLevelSelectScene = new Scene();
		this.mLevelSelectScene.setBackground(new ColorBackground(0.2f, 0.2f, 0.5f));

		this.mScrollDetector = new SurfaceScrollDetector(this);
		this.mClickDetector = new ClickDetector(this);

		this.mLevelSelectScene.setOnSceneTouchListener(this);
		this.mLevelSelectScene.setTouchAreaBindingEnabled(true);
		this.mLevelSelectScene.setOnSceneTouchListenerBindingEnabled(true);

		// calculate the amount of required columns for the level count
		int totalRows = (LEVELS / LEVEL_COLUMNS_PER_SCREEN) + 1;

		// Calculate space between each level square
		int spaceBetweenRows = (CAMERA_HEIGHT / LEVEL_ROWS_PER_SCREEN) - LEVEL_PADDING;
		int spaceBetweenColumns = (CAMERA_WIDTH / LEVEL_COLUMNS_PER_SCREEN) - LEVEL_PADDING;

		//Set the wood Background
		for (int x = 0; x < CAMERA_WIDTH; x += BACKGROUND_TILE_SIZE) {
			for (int y = 0; y < (totalRows*150); y += BACKGROUND_TILE_SIZE) {
				Sprite mBackground = new Sprite(x, y, BACKGROUND_TILE_SIZE, BACKGROUND_TILE_SIZE, this.mBackgroundTextureRegion);
				this.mLevelSelectScene.attachChild(mBackground);
				//spriteList.add(mBackground);
			}
		}
		
 		// Current Level Counter
		int iLevel = 1;

		// Create the Level selectors, one row at a time.
		int boxX = LEVEL_PADDING, boxY = LEVEL_PADDING;
		for (int y = 0; y < totalRows; y++) {
			for (int x = 0; x < LEVEL_COLUMNS_PER_SCREEN; x++) {

				// On Touch, save the clicked level in case it's a click and not
				// a scroll.
				final int levelToLoad = iLevel;

				// Create the rectangle. If the level selected
				// has not been unlocked yet, don't allow loading.
				Sprite box = new Sprite(boxX, boxY, mLevelSelectRegion) {
					@Override
					public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
						if (levelToLoad >= mMaxLevelReached)
							iLevelClicked = -1;
						else
							iLevelClicked = levelToLoad;
						return false;
					}
				};
				
				box.setScale(1.5f);
 
				this.mLevelSelectScene.attachChild(box);

				// Center for different font size
				if (iLevel < 10) {
					this.mLevelSelectScene.attachChild(new Text(boxX + 17, boxY + 3, this.mFont, String.valueOf(iLevel)));
				} else {
					this.mLevelSelectScene.attachChild(new Text(boxX + 4, boxY + 3, this.mFont, String.valueOf(iLevel)));
				}

				this.mLevelSelectScene.registerTouchArea(box);

				iLevel++;
				boxX += spaceBetweenColumns + LEVEL_PADDING;

				if (iLevel > LEVELS)
					break;
			} 

			if (iLevel > LEVELS)
				break;

			boxY += spaceBetweenRows + LEVEL_PADDING;
			boxX = 50;
		}

		// Set the max scroll possible, so it does not go over the boundaries.
		mMaxY = boxY - CAMERA_HEIGHT + 200;


		
		return this.mLevelSelectScene;
	}

	// Here is where you call the level load
	@Override
	public void loadLevel(final int iLevel) {
		if (iLevel != -1) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(MazzterActivity.this, "Loading level " + String.valueOf(iLevel), Toast.LENGTH_SHORT).show();
					mEngine.vibrate(200);

					iLevelClicked = -1;
					mCurrentY = 0;
					
					isLevelSelecting = false;
					
					
					//mCamera.reset();
					
					mCurrentLevel = iLevel;
					
					mEngine.getTextureManager().reloadTextures();
					mEngine.getFontManager().reloadFonts();
					LoadMaze(mScene);
					mEngine.getTextureManager().reloadTextures();
					mEngine.getFontManager().reloadFonts();
					
					mEngine.setScene(mScene);
					
					mCamera.setCenter(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2);
					
					setAdVisibility(false);
					//
					
				}
			});
		}
	}
	


}
