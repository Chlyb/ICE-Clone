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
import com.mygdx.game.gameSession.SinglePlayerSession;

public class SinglePlayerMenu extends AbstractScreen {
    private MainMenuScreen mainMenu;

    public SinglePlayerMenu(MyGdxGame game, MainMenuScreen mainMenu){
        super(game, mainMenu);
        this.mainMenu = mainMenu;
        init();
    }

    private void init(){
        Label gameTitle = new Label("Single player", skin, "big");
        gameTitle.setSize(120,100);
        gameTitle.setPosition(420,450);
        gameTitle.setAlignment(Align.center);
        stage.addActor(gameTitle);

        Label playercolorLabel = new Label("Player color", skin, "default");
        playercolorLabel.setSize(120,40);
        playercolorLabel.setPosition(200,340);
        playercolorLabel.setAlignment(Align.center);
        stage.addActor(playercolorLabel);

        final SelectBox<String> playercolorBox = new SelectBox<String>(skin);
        playercolorBox.setItems("Green","Blue","Purple","Orange","Red");
        playercolorBox.setSize(120,40);
        playercolorBox.setPosition(200,300);
        stage.addActor(playercolorBox);

        Label enemiescountLabel = new Label("Number of enemies", skin, "default");
        enemiescountLabel.setSize(120,40);
        enemiescountLabel.setPosition(420,340);
        enemiescountLabel.setAlignment(Align.center);
        stage.addActor(enemiescountLabel);

        final SelectBox<Integer> enemiescountBox = new SelectBox<Integer>(skin);
        enemiescountBox.setItems(1,2,3,4);
        enemiescountBox.setSize(120,40);
        enemiescountBox.setPosition(420,300);
        stage.addActor(enemiescountBox);

        Label flagcountLabel = new Label("Number of flags", skin, "default");
        flagcountLabel.setSize(120,40);
        flagcountLabel.setPosition(620,340);
        flagcountLabel.setAlignment(Align.center);
        stage.addActor(flagcountLabel);

        final SelectBox<Integer> flagcountBox = new SelectBox<Integer>(skin);
        flagcountBox.setItems(5,6,7,8,9,10,11,12,13,14,15);
        flagcountBox.setSize(120,40);
        flagcountBox.setPosition(620,300);
        stage.addActor(flagcountBox);

        final Button playBtn = new TextButton("Play", skin, "default");
        playBtn.setSize(150,50);
        playBtn.setPosition(415,230);

        final Label errorLabel = new Label("", skin, "default");

        playBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if(enemiescountBox.getSelected() >= flagcountBox.getSelected()){
                    errorLabel.setText("Too many enemies");
                }
                else{
                    SinglePlayerSession sps = new SinglePlayerSession(game, getThis(), flagcountBox.getSelected(), enemiescountBox.getSelected(), playercolorBox.getSelected());
                    game.setScreen(sps);
                    Gdx.input.setInputProcessor(sps.getInputMultiplexer());
                }
                System.out.println("to the game");
            }
        });
        stage.addActor(playBtn);

        final Button exitBtn = new TextButton("Back", skin, "default");
        exitBtn.setSize(150,50);
        exitBtn.setPosition(415,90);

        exitBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                goBack();
            }
        });
        stage.addActor(exitBtn);

        errorLabel.setSize(150,40);
        errorLabel.setPosition(600,170);
        errorLabel.setAlignment(Align.center);
        stage.addActor(errorLabel);
    }

    SinglePlayerMenu getThis(){
        return this;
    }

    @Override
    public void render(float delta) {
        clearScreen();
        super.render(delta);
    }
}
