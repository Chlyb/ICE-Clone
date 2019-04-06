package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.menu.MainMenuScreen;

//public class MyGdxGame extends ApplicationAdapter {
public class MyGdxGame extends Game {
	public static final String TITLE = "ICE Clone";
	//public static final int WIDTH = 1920;
	//public static final int HEIGHT = 1080;
	public int WIDTH;
	public int HEIGHT;

	public ShapeRenderer sr;
	public SpriteBatch sb;
	public BitmapFont bf;

	private  boolean paused;
	
	@Override
	public void create () {
		WIDTH = Gdx.graphics.getWidth();
		HEIGHT = Gdx.graphics.getHeight();

		sr = new ShapeRenderer();
		sb = new SpriteBatch();
		bf = new BitmapFont(true);

		this.setScreen(new MainMenuScreen(this));
	}
	
	@Override
	public void dispose () {
	}

	public void setPaused(boolean b) {
	}
}