package com.mygdx.game.gameSession;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Timer;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.gameClasses.Flag;
import com.mygdx.game.gameClasses.GamePacket;
import com.mygdx.game.gameClasses.Team;
import com.mygdx.game.menu.AbstractScreen;
import com.mygdx.game.menu.ResumeScreen;
import com.mygdx.game.menu.SinglePlayerMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public final class SinglePlayerSession extends AbstractSession {
    private GamePacket gp;
    private Team playerTeam;
    private AtomicLong physicsTime = new AtomicLong(0);
    private SinglePlayerMenu spm;

    public SinglePlayerSession(MyGdxGame game, SinglePlayerMenu singlePlayerMenu, int flagCount, int enemyCount, String playerColor) {
        super(game, null, 1);
        previousScreen = new ResumeScreen(game, singlePlayerMenu, this);
        this.spm = singlePlayerMenu;

        this.gp = new GamePacket();

        List<String> existingTeams = new ArrayList<String>();

        if (playerColor == "Green") {
            playerTeam = new Team(gp, 1);
            existingTeams.add("Green");
        } else if (playerColor == "Blue") {
            playerTeam = new Team(gp, 2);
            existingTeams.add("Blue");
        } else if (playerColor == "Purple") {
            playerTeam = new Team(gp, 3);
            existingTeams.add("Purple");
        } else if (playerColor == "Orange") {
            playerTeam = new Team(gp, 4);
            existingTeams.add("Orange");
        } else if (playerColor == "Red") {
            playerTeam = new Team(gp, 5);
            existingTeams.add("Red");
        }

        Team.generateTeams(gp, existingTeams, enemyCount);
        Flag.generateFlags(gp, 1920, 1080, flagCount);

        physicsTime.set(System.currentTimeMillis());

        final Runnable copyToRender = new Runnable() {
            @Override
            public void run() {
                renderedGp = gp.clone();
                renderedGp.goOneTickBack();
                updateUI();
                renderTime.set(System.currentTimeMillis());
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() { //Update thread
                while (!finished) {
                    if (!paused.get()){
                        gp.update(physicsTime);
                        for (Team team : gp.getTeams()) {
                            if (team != gp.getNeutralTeam() && team != playerTeam) team.AItrigger();
                        }
                        new Thread(copyToRender).start();
                    }
                }
            }
        }).start();
    }

    @Override
    void end(boolean definitive) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                finishing = true;
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                dispose();
            }
        }).start();
    }

    @Override
    protected void goBack(){
        paused.set(true);
        super.goBack();
    }

    @Override
    public void unpause() {
        super.unpause();
        physicsTime.set(System.currentTimeMillis());
    }

    @Override
    protected void playerClick(float x, float y){
        playerTeam.onPress((int) (x * zoom + gameCam.position.x - game.WIDTH / 2 * zoom), (int) (y * zoom + gameCam.position.y - game.HEIGHT / 2 * zoom));
    }

    @Override
    protected void upgrade(int upgrade) {
        playerTeam.upgrade(upgrade);
    }

    @Override
    public void dispose(){
        finished = true;
        super.dispose();
        game.setScreen( spm);
        Gdx.input.setInputProcessor(spm.getInputMultiplexer());
    }
}
