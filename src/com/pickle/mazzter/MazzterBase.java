package com.pickle.mazzter;

import org.anddev.andengine.audio.music.Music;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.camera.SmoothCamera;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.menu.MenuScene;
import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
import org.anddev.andengine.extension.physics.box2d.util.Vector2Pool;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.input.touch.detector.ClickDetector;
import org.anddev.andengine.input.touch.detector.ClickDetector.IClickDetectorListener;
import org.anddev.andengine.input.touch.detector.ScrollDetector;
import org.anddev.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.anddev.andengine.input.touch.detector.SurfaceScrollDetector;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.sensor.accelerometer.AccelerometerData;
import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
import org.anddev.andengine.ui.activity.LayoutGameActivity;

import android.view.KeyEvent;
import android.view.Menu;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.scoreloop.client.android.core.model.Session;
import com.scoreloop.client.android.ui.OnStartGamePlayRequestObserver;
import com.scoreloop.client.android.ui.ScoreloopManager;
import com.scoreloop.client.android.ui.ScoreloopManagerSingleton;

public class MazzterBase extends LayoutGameActivity implements MazzterConstants, IAccelerometerListener, IOnSceneTouchListener, IOnMenuItemClickListener,
IScrollDetectorListener, IClickDetectorListener , OnStartGamePlayRequestObserver {

	// ===========================================================
	// Constants
	// ===========================================================
	protected static int CAMERA_WIDTH = 720;
	protected static int CAMERA_HEIGHT = 480;
	protected static int BACKGROUND_TILE_SIZE = 128;

	protected static final int MENU_RESET = 0;
	protected static final int MENU_OK = MENU_RESET + 1;
	protected static final int MENU_NEXT_LEVEL = MENU_OK + 1;
	protected static final int MENU_SKIP = MENU_NEXT_LEVEL + 1;

	protected static final int LEVEL_COUNT = 20;
	
	protected static int LEVELS = LEVEL_COUNT;
	protected static int LEVEL_COLUMNS_PER_SCREEN = 4;
	protected static int LEVEL_ROWS_PER_SCREEN = 3;
	protected static int LEVEL_PADDING = 50;
	
	// ===========================================================
	// Fields
	// ===========================================================
	
	protected Scene mScene;
	protected Scene mLevelSelectScene;
	protected MenuScene mMenuScene;

	protected Camera mCamera;
	protected PhysicsWorld mPhysicsWorld;
	protected Music mMusic;
	protected Font mFont;

	protected AdView mAdView;

	
	protected int mCurrentLevel;
	
	// Scrolling
	protected SurfaceScrollDetector mScrollDetector;
	protected ClickDetector mClickDetector;

	protected float mMinY = 0;
	protected float mMaxY = 0;
	protected float mCurrentY = 0;
	protected int iLevelClicked = -1;
	
	
	protected int mMaxLevelReached = 21;

	protected Boolean isLevelSelecting = false;
	
	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	@Override
	protected int getLayoutID() {
		return R.layout.main;
	}

	@Override
	protected int getRenderSurfaceViewID() {
		return R.id.xmllayoutexample_rendersurfaceview;
	}

	@Override
	public void onLoadComplete() {

		//Init AdMob
		mAdView = (AdView) this.findViewById(R.id.adView);
		mAdView.refreshDrawableState();

		mAdView.setVisibility(AdView.VISIBLE);
		AdRequest adRequest = new AdRequest();
		mAdView.loadAd(adRequest);
	
		
		//Init ScoreLoop
		MazzterApplication.setGamePlaySessionStatus(MazzterApplication.GamePlaySessionStatus.NORMAL);
		
		final int mode = 0 + Session.getCurrentSession().getGame().getMinMode();
		MazzterApplication.setGamePlaySessionMode(mode);
		
		final ScoreloopManager manager = ScoreloopManagerSingleton.get();
		if (!manager.hasLoadedAchievements()) {
			manager.loadAchievements(new Runnable() {
				@Override
				public void run() {
					
				}
			});
		}
		
	}

	
	@Override
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
		if (isLevelSelecting) {
			this.mClickDetector.onTouchEvent(pSceneTouchEvent);
			this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
		}
		return true;
	}

	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
		if (pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
			if (this.mScene.hasChildScene()) {
				/* Remove the menu and reset it. */
				this.mMenuScene.back();
			} else {
				/* Attach the menu. */
				// this.mScene.setChildScene(this.mMenuScene, false, true,
				// true);
			}
			return true;
		} else {
			return super.onKeyDown(pKeyCode, pEvent);
		}
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu) {
		return super.onCreateOptionsMenu(pMenu);
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu pMenu) {
		return super.onPrepareOptionsMenu(pMenu);
	}

	@Override
	public Engine onLoadEngine() {
		return null;
	}

	@Override
	public void onLoadResources() {
	}

	@Override
	public Scene onLoadScene() {
		return null;
	}

	@Override
	public void onAccelerometerChanged(final AccelerometerData pAccelerometerData) {
		final Vector2 gravity = Vector2Pool.obtain(pAccelerometerData.getX() * 2.5f, pAccelerometerData.getY() * 2.5f);
		// On some coby tablets, the accelerometer is not properly aligned:
		// final Vector2 gravity = Vector2Pool.obtain(pAccelerometerData.getY()
		// * 2.5f, pAccelerometerData.getX() * -2.5f);
		this.mPhysicsWorld.setGravity(gravity);
		Vector2Pool.recycle(gravity);
	}

	@Override
	public void onResumeGame() {
		super.onResumeGame();
		// this.mMusic.play();
		this.enableAccelerometerSensor(this);
	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();
		// this.mMusic.stop();
		this.disableAccelerometerSensor();

		mAdView.setVisibility(AdView.INVISIBLE);
	}

	protected void setAdVisibility(Boolean show) {
		if (!show) {
			MazzterBase.this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mAdView.setVisibility(AdView.INVISIBLE);
				}
			});
		} else {

			MazzterBase.this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mAdView.setVisibility(AdView.VISIBLE);
					AdRequest adRequest = new AdRequest();
					mAdView.loadAd(adRequest);

				}
			});

		}

	}

	@Override
	public boolean onMenuItemClicked(MenuScene arg0, IMenuItem arg1, float arg2, float arg3) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onStartGamePlayRequest(Integer mode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScroll(final ScrollDetector pScollDetector, final TouchEvent pTouchEvent, final float pDistanceX, final float pDistanceY) {

		if ( ((mCurrentY - pDistanceY) < mMinY) || ((mCurrentY - pDistanceY) > mMaxY) )
			return;

		this.mCamera.offsetCenter(0, -pDistanceY);

		mCurrentY -= pDistanceY;

	}

	@Override
	public void onClick(ClickDetector pClickDetector, TouchEvent pTouchEvent) {
		loadLevel(iLevelClicked);
	};
	
	public void loadLevel(int iLevel) {
	}
	
	
}