package com.mygdx.game.gameSession;

import com.mygdx.game.MyGdxGame;
import com.mygdx.game.gameClasses.Flag;
import com.mygdx.game.gameClasses.GamePacket;
import com.mygdx.game.gameClasses.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public final class SinglePlayerSession extends AbstractSession {
    private GamePacket gp;
    private Team playerTeam;
    private AtomicLong physicsTime = new AtomicLong(0);

    public SinglePlayerSession(MyGdxGame game, int flagCount, int enemyCount, String playerColor) {
        super(game, 1);
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
                while (true) {
                    gp.update(physicsTime);
                    for (Team team : gp.getTeams()) {
                        if (team != gp.getNeutralTeam() && team != playerTeam) team.AItrigger();
                    }
                    new Thread(copyToRender).start();
                }
            }
        }).start();
    }

    @Override
    protected void playerClick(float x, float y){
        playerTeam.onPress((int) (x * zoom + gameCam.position.x - game.WIDTH / 2 * zoom), (int) (y * zoom + gameCam.position.y - game.HEIGHT / 2 * zoom));
    }

    @Override
    protected void upgrade(int upgrade) {
        playerTeam.upgrade(upgrade);
    }
}
