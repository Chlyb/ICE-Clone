package com.mygdx.game.gameSession;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.gameClasses.GamePacket;
import com.mygdx.game.gameClasses.Team;
import com.mygdx.game.menu.AbstractScreen;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractSession extends AbstractScreen implements GestureDetector.GestureListener {
    protected float zoom;
    protected final OrthographicCamera gameCam = new OrthographicCamera();

    private Label attackLabel;
    private Label healthLabel;
    private Label speedLabel;
    private Label upgradeLabel;
    private Label endLabel;

    protected final int playerTeamIndex;
    private int color;
    private int progressBarWidth;

    protected GamePacket renderedGp;
    protected AtomicLong renderTime;
    private short shootOffset;

    protected AtomicBoolean paused;
    protected boolean finishing = false;
    protected boolean finished = false;

    public AbstractSession(MyGdxGame game, AbstractScreen previousScreen, int playerTeamIndex) {
        super(game, previousScreen);

        gameCam.setToOrtho(true);
        gameCam.position.x = game.WIDTH/2;
        gameCam.position.y = game.HEIGHT/2;
        zoom = 1;
        gameCam.update();

        renderedGp = new GamePacket();
        renderTime = new AtomicLong(0);
        shootOffset = 0;

        this.playerTeamIndex = playerTeamIndex;
        progressBarWidth = 0;

        init();

        GestureDetector gestureDetector = new GestureDetector(this);
        inputMultiplexer.addProcessor(gestureDetector);
        paused = new AtomicBoolean(false);
    }

    private void init(){
        attackLabel  = new Label("ATTACK:", skin, "big");
        attackLabel.setPosition(70,470);
        attackLabel.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                upgrade(0);
            }
        });
        stage.addActor(attackLabel);

        healthLabel = new Label("HEALTH:", skin, "big");
        healthLabel.setPosition(407,470);
        healthLabel.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                upgrade(1);
            }
        });
        stage.addActor(healthLabel);

        speedLabel = new Label("SPEED:", skin, "big");
        speedLabel.setPosition(720,470);
        speedLabel.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                upgrade(2);
            }
        });
        stage.addActor(speedLabel);

        upgradeLabel = new Label("UPGRADE!", skin, "big");
        upgradeLabel.setSize(0,0);
        upgradeLabel.setPosition(950,440);
        upgradeLabel.setAlignment(Align.right);
        stage.addActor(upgradeLabel);

        endLabel = new Label("", skin, "big");
        endLabel.setSize(0,0);
        endLabel.setPosition(480,280);
        endLabel.setAlignment(Align.center);
        stage.addActor(endLabel);
    }

    protected void updateUI(){
        if(renderedGp.getTeams().size() > playerTeamIndex) {
            progressBarWidth = renderedGp.getTeams().get(playerTeamIndex).getProgressBarWidth();
            color = renderedGp.getTeams().get(playerTeamIndex).color;

            attackLabel.setText("ATTACK: " + renderedGp.getTeams().get(playerTeamIndex).getAttackLevel());
            healthLabel.setText("HEALTH: " + renderedGp.getTeams().get(playerTeamIndex).getHealthLevel());
            speedLabel.setText("SPEED: " + renderedGp.getTeams().get(playerTeamIndex).getSpeedLevel());

            if(renderedGp.getTeams().get(playerTeamIndex).getAvailableUpgrades() > 0){
                if(renderedGp.getTeams().get(playerTeamIndex).getAvailableUpgrades() == 1) upgradeLabel.setText("1 UPGRADE!");
                else upgradeLabel.setText( renderedGp.getTeams().get(playerTeamIndex).getAvailableUpgrades() + " UPGRADES!");
                upgradeLabel.setVisible(true);
            }
            else upgradeLabel.setVisible(false);

            if(!finishing){
                for (Team team : renderedGp.getTeams()){
                    if(team.getFlagCount() == renderedGp.getFlags().size()) end(true);
                }
                if(renderedGp.getTeams().get(playerTeamIndex).getFlagCount() == 0){
                    endLabel.setText("GAME OVER");
                    end(false);
                }
                else if(renderedGp.getTeams().get(playerTeamIndex).getFlagCount() == renderedGp.getFlags().size()){
                    endLabel.setText("VICTORY!");
                    end(true);
                }
            }
        }
    }

    abstract void end(boolean definitive);

    @Override
    public void render(float delta){
        ++shootOffset;
        shootOffset %= 4;
        renderedGp.render( (System.currentTimeMillis() - renderTime.get())/1000.0f, gameCam, game.sr, game.sb, game.bf, playerTeamIndex, shootOffset);

        game.sr.setProjectionMatrix(viewport.getCamera().combined);
        game.sr.begin(ShapeRenderer.ShapeType.Filled);
        game.sr.setColor(GamePacket.colors[color]);
        game.sr.rect(0,540, progressBarWidth, -10);

        for(int i = 1 ; i < renderedGp.getTeams().size(); ++i) {
            Team tmpTeam = renderedGp.getTeams().get(i);
            game.sr.setColor( GamePacket.colors[ tmpTeam.color]);
            game.sr.rectLine(21 , 550 - i*90,79, 550 - i*90, 8);
            game.sr.rectLine(21 , 500 - i*90,79, 500 - i*90, 8);
            game.sr.rectLine(25 , 500 - i*90,25, 550 - i*90, 8);
            game.sr.rectLine(75 , 500 - i*90,75, 550 - i*90, 8);

            game.sb.setProjectionMatrix(viewport.getCamera().combined);
            game.sb.begin();
            game.menuBf.setColor( GamePacket.colors[ tmpTeam.color]);
            GlyphLayout glyphLayout = new GlyphLayout();
            glyphLayout.setText(game.menuBf, Integer.toString( tmpTeam.getFlagCount()));
            game.menuBf.draw(game.sb, glyphLayout, 100 , 543 - i*90);
            game.sb.end();
        }
        game.sr.end();

        super.render(0);
    }

    protected abstract void playerClick(float x, float y);
    protected abstract void upgrade(int upgrade);

    public void unpause(){
        paused.set(false);
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if (y > 0.13 * game.HEIGHT) playerClick(x, y);
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        gameCam.translate(-deltaX*zoom, -deltaY*zoom);
        gameCam.update();
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        gameCam.zoom = zoom * initialDistance/distance;
        gameCam.update();
        return false;
    }

    @Override
    public void pinchStop() {
        zoom = gameCam.zoom;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) { return false; }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }
}
