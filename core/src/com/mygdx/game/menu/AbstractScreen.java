package com.mygdx.game.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
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

public abstract class AbstractScreen implements Screen, InputProcessor {
    protected MyGdxGame game;
    protected Stage stage;
    protected OrthographicCamera camera;
    protected Viewport viewport;
    protected SpriteBatch spriteBatch;

    protected Skin skin;

    protected final InputMultiplexer inputMultiplexer;
    protected AbstractScreen previousScreen;

    public AbstractScreen(MyGdxGame game, AbstractScreen previousScreen){
        this.game = game;
        this.previousScreen = previousScreen;
        createCamera();
        viewport = new StretchViewport(960,540,camera);
        stage = new Stage(viewport);
        spriteBatch = new SpriteBatch();
        //skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        skin = new Skin(Gdx.files.internal("data/glassy/glassy-ui.json"));
        skin.add("pixelFont", game.bf, BitmapFont.class);

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(this);
    }

    private void createCamera(){
        camera = new OrthographicCamera();
        camera.update();
    }

    protected void clearScreen(){
        Gdx.gl.glClearColor(0,0,0,0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    public InputMultiplexer getInputMultiplexer(){
        return inputMultiplexer;
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

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Input.Keys.BACK || keycode == Input.Keys.E){
            if(previousScreen == null) Gdx.app.exit();
            else{
                game.setScreen(previousScreen);
                Gdx.input.setInputProcessor(previousScreen.getInputMultiplexer());
            }
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }
}
