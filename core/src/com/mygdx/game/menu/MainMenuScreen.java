package com.mygdx.game.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.mygdx.game.MyGdxGame;

public class MainMenuScreen extends AbstractScreen {
    private String arg;
    private SinglePlayerMenu lps;
    private MultiplayerMenuScreen mms;

    public MainMenuScreen(MyGdxGame game){
        super(game);
        lps = new SinglePlayerMenu( game, this);
        mms = new MultiplayerMenuScreen( game, this);
        init();
    }

    private void init(){
        Label gameTitle = new Label("ICE", skin, "big");
        gameTitle.setSize(120,100);
        gameTitle.setPosition(420,450);
        gameTitle.setAlignment(Align.center);
        stage.addActor(gameTitle);

        final Button singleplayerBtn = new TextButton("Single player", skin, "default");
        singleplayerBtn.setSize(350,50);
        singleplayerBtn.setPosition(320,330);
        singleplayerBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                Gdx.input.setInputProcessor(lps.stage);
                game.setScreen(lps);
            }
        });
        stage.addActor(singleplayerBtn);

        final Button multiplayerBtn = new TextButton("Multiplayer", skin, "default");
        multiplayerBtn.setSize(340,50);
        multiplayerBtn.setPosition(320,260);
        multiplayerBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                Gdx.input.setInputProcessor(mms.stage);
                mms.refreshIP();
                mms.refreshServers();
                game.setScreen(mms);
            }
        });
        stage.addActor(multiplayerBtn);

        final Button exitBtn = new TextButton("Exit", skin, "default");
        exitBtn.setSize(140,50);
        exitBtn.setPosition(420,190);
        exitBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                Gdx.app.exit();
            }
        });
        stage.addActor(exitBtn);

        Gdx.input.setInputProcessor(stage);
    }

    public  void render(float delta){
        super.render(delta);
    }
}
