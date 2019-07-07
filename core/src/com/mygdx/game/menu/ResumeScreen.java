package com.mygdx.game.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.gameSession.AbstractSession;
import com.mygdx.game.gameSession.SinglePlayerSession;

public class ResumeScreen extends AbstractScreen {
    private final AbstractScreen previousScreen; //overriden because back button leads back to session
    private final AbstractSession session;

    public ResumeScreen(MyGdxGame game, AbstractScreen previousScreen, AbstractSession session){
        super(game, session);
        this.previousScreen = previousScreen;
        this.session = session;
        init();
    }

    private void init(){
        final Button resumeBtn = new TextButton("Resume", skin, "default");
        resumeBtn.setSize(250,50);
        resumeBtn.setPosition(355,320);

        resumeBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                    game.setScreen(session);
                    Gdx.input.setInputProcessor(session.getInputMultiplexer());
                    session.unpause();
            }
        });
        stage.addActor(resumeBtn);

        final Button exitBtn = new TextButton("Exit", skin, "default");
        exitBtn.setSize(250,50);
        exitBtn.setPosition(355,240);

        exitBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                session.dispose();
            }
        });
        stage.addActor(exitBtn);
    }

    @Override
    protected void goBack(){
        super.goBack();
        session.unpause();
    }

    @Override
    public void render(float delta) {
        clearScreen();
        super.render(delta);
    }
}
