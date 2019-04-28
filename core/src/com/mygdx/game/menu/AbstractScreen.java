package com.mygdx.game.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.game.MyGdxGame;

public abstract class AbstractScreen implements Screen {
    protected MyGdxGame game;
    protected Stage stage;
    protected OrthographicCamera camera;
    protected Viewport viewport;
    protected SpriteBatch spriteBatch;

    protected Skin skin;

    public AbstractScreen(MyGdxGame game){
        this.game = game;
        createCamera();
        viewport = new StretchViewport(960,540,camera);
        stage = new Stage(viewport);
        spriteBatch = new SpriteBatch();
        //skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        skin = new Skin(Gdx.files.internal("data/glassy/glassy-ui.json"));
        skin.add("pixelFont", game.bf, BitmapFont.class);
    }

    private void createCamera(){
        camera = new OrthographicCamera();
        camera.update();
    }

    @Override
    public void render(float delta){
        camera.update();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        stage.act();
        stage.draw();
    }

    @Override
    public void show(){
    }

    @Override
    public void hide(){
    }

    @Override
    public void pause(){
        game.setPaused(true);
    }

    @Override
    public void resume(){
        game.setPaused(false);
    }

    @Override
    public void resize(int width, int height){

    }

    @Override
    public void dispose(){

    }

    protected void clearScreen(){
        Gdx.gl.glClearColor(0,0,0,0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

}
