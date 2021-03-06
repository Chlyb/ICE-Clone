package com.mygdx.game.gameClasses;

import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Team implements Serializable {
    transient public final GamePacket gp;
    public final int color;

    transient private int damage;
    transient private int health;
    transient private int flagHealth;
    private int speed;

    transient private int score;
    transient private int scoreToNextLevel;
    private int availableUpgrades;
    private int progressBarWidth;
    transient private int level;
    private int attackLevel;
    private int healthLevel;
    private int speedLevel;

    private List<Objective> objectives;
    transient private List<Ship> ships;
    transient private List<Flag> flags;
    transient private List<Flag> linkedFlags;
    transient public int objectiveCount;
    transient private int shipCount;
    transient private int ix;
    private int flagCount;

    transient private int iteAI;
    transient private int timeSinceAttack;

    //AI
    transient private List<Objective> offensiveObjectives;
    transient private List<Objective> defensiveObjectives;

    public Team(GamePacket gp, int color) {
        this.gp = gp;
        this.color = color;

        damage = 5;
        health = 5;
        flagHealth = 1000;
        speed = 50;

        score = 0;
        scoreToNextLevel = 50;
        progressBarWidth = 0;
        availableUpgrades = 0;
        level = 3;
        attackLevel = 1;
        healthLevel = 1;
        speedLevel = 1;

        objectives = new ArrayList<Objective>();
        ships = new ArrayList<Ship>();
        flags = new ArrayList<Flag>();
        linkedFlags = new ArrayList<Flag>();
        objectiveCount = 0;
        shipCount = 0;
        flagCount = 0;
        ix = 0;

        iteAI = (int)(Math.random() * 100);

        gp.addTeam(this);
    }

    public void update(){
        while (score > scoreToNextLevel) {
            score = score - scoreToNextLevel;
            ++availableUpgrades;
            ++level;
            scoreToNextLevel += 100;

            if(level == 30){
                score = -2147483647;
                scoreToNextLevel = 2147483647;
            }
        }
        progressBarWidth = 960 * score / scoreToNextLevel;
    }

    public void upgrade(int upgrade){
        if(availableUpgrades > 0){
            if(upgrade == 0 && attackLevel < 10){
                ++damage;
                ++attackLevel;
                --availableUpgrades;
                ++level;
            }
            else if(upgrade == 1 && healthLevel < 10){
                ++health;
                if(flagHealth == 9) flagHealth = 2000;
                else flagHealth += 111;
                ++healthLevel;
                --availableUpgrades;
                ++level;
            }
            else if(upgrade == 2 && speedLevel < 10){
                speed += 50;
                ++speedLevel;
                --availableUpgrades;
                ++level;
            }
        }
    }

    public void onPress(int x, int y) {
        Boolean deleted = false;
        for (Objective objective : objectives) {

            if (Math.pow((objective.getX() - x), 2) + Math.pow((objective.getY() - y), 2) <= Math.pow(objective.RADIUS, 2)) {
                objective.delete();
                deleted = true;
                break;
            }
        }
        if (!deleted) {
            Vector2 v = new Vector2(x, y);
            new Objective(this, v);
        }
    }

    public void reassignObjectives() {
        if (objectiveCount != 0) {
            for (int i = 0; i < shipCount; i += objectiveCount) {
                ships.get(i).setObjective(objectives.get(objectiveCount - 1));
            }
        } else {
            for (Ship ship : ships) {
                ship.setObjective(null);
            }
        }
    }

    public void assignObjectives() {
        if (objectiveCount != 0) {
            for (Ship ship : ships) {
                if (ship.getObjective() == null) {
                    ++ix;
                    ix%=objectiveCount;
                    ship.setObjective(objectives.get(ix));
                }
            }
        } else {
            for (Ship ship : ships) {
                ship.setObjective(null);
            }
        }
    }

    public Objective giveObjective() {
        if (objectiveCount != 0) {
            ++ix;
            ix %= objectiveCount;
            return objectives.get(ix);
        }
        else return null;
    }

    public void updateLinkedFlags(){ //#it can be significantly optimised but I'm lazy
        linkedFlags.clear();
        for (Flag flag : flags) {
            for (Flag flag2 : flag.getLinkedFlags()) {
                if (flag2.getTeam() != this && !linkedFlags.contains(flag2)){
                    linkedFlags.add(flag2);
                }
            }
        }
    }

    public void addShip(Ship ship){
        ++shipCount;
        ships.add(ship);
    }

    public void removeShip(Ship ship){
        --shipCount;
        ships.remove(ship);
    }

    public void addFlag(Flag flag){
        ++flagCount;
        flags.add(flag);
    }

    public void removeFlag(Flag flag){
        --flagCount;
        flags.remove(flag);
    }

    public void addObjective(Objective objective){
        ++objectiveCount;
        objectives.add(objective);
        reassignObjectives();
    }

    public void removeObjective(Objective objective){
        for(Ship ship : ships){
            if(ship.getObjective() == objective) ship.setObjective(null);
        }
        --objectiveCount;
        objectives.remove(objective);
        assignObjectives();
    }

    public void removeTarget(Flag flag){
        for(Ship ship : ships){
            if(ship.getTarget() == flag) ship.setTarget(null);
        }
    }

    public int getDamage(){return damage;}
    public int getHealth(){return health;}
    public int getSpeed(){return speed;}

    public int getAttackLevel(){return attackLevel;}
    public int getHealthLevel(){return healthLevel;}
    public int getFlagHealth(){return flagHealth;}
    public int getSpeedLevel(){return speedLevel;}
    public int getProgressBarWidth(){return progressBarWidth;}
    public int getAvailableUpgrades(){return availableUpgrades;}
    public void setHealth(int h){health = h;}
    public void addScore(float score){ this.score += score;}

    public int getShipCount(){
        return shipCount;
    }
    public int getFlagCount(){
        return flagCount;
    }

    public List<Objective> getObjectives(){
        return objectives;
    }
    public List<Flag> getFlags(){
        return flags;
    }

    public Boolean spawnShip(){
        if(shipCount < 25 * flagCount) return true;
        return false;
    }

    public static void generateTeams(GamePacket gp, List<String> existingTeams, int enemyCount){
        int currentenemyCount = 0;
        while(currentenemyCount < enemyCount){
            int x = (int)(Math.random() * 5);
            System.out.println(x);
            if(x == 0){
                if(!existingTeams.contains("Green")){
                    new Team(gp, 1);
                    existingTeams.add("Green");
                    ++currentenemyCount;
                }
            }
            else if(x == 1){
                if(!existingTeams.contains("Blue")){
                    new Team(gp, 2);
                    existingTeams.add("Blue");
                    ++currentenemyCount;
                }
            }
            else if(x == 2){
                if(!existingTeams.contains("Purple")){
                    new Team(gp, 3);
                    existingTeams.add("Purple");
                    ++currentenemyCount;
                }
            }
            else if(x == 3){
                if(!existingTeams.contains("Orange")){
                    new Team(gp, 4);
                    existingTeams.add("Orange");
                    ++currentenemyCount;
                }
            }
            else if(x == 4){
                if(!existingTeams.contains("Red")){
                    new Team(gp, 5);
                    existingTeams.add("Red");
                    ++currentenemyCount;
                }
            }
        }
    }

    public void AItrigger(){
        if(iteAI < 0){
            updateAI();
            iteAI = 100;
        }
        else --iteAI;
    }

    public void updateAI(){
        //defend
        List<Flag> flagsToDefend = new ArrayList<Flag>();
        int enemysFlagCount = 0;

        for(Team team : gp.getTeams()){
            if(team != this && team.flagCount > 0){
                for (Objective objective : team.getObjectives()){
                    if(flags.contains( objective.getTargetedFlag())) {
                        flagsToDefend.add( objective.getTargetedFlag());
                        enemysFlagCount += team.flagCount;
                        this.onPress( (int)team.getFlags().get(0).getPos().x, (int)team.getFlags().get(0).getPos().y);
                    }
                }
            }
        }

        if(flagsToDefend.size() > 0){
            List<Objective> objectivesToDeletion = new ArrayList<Objective>();
            if(enemysFlagCount >= flagCount){
                for(Objective objective : objectives){ //delete unwanted objectives
                    if( !flagsToDefend.contains( objective.getTargetedFlag())){
                        objectivesToDeletion.add(objective);
                    }
                }
            }
            for(Objective objective : objectivesToDeletion){ //delete unwanted objectives
                this.onPress( (int)objective.getX(), (int)objective.getY());
            }

            for(Flag flag : flagsToDefend){//adding defensive objectives
                if( !flagIsInObjectives(flag)){
                    this.onPress( (int)flag.getPos().x, (int)flag.getPos().y);
                }
            }
        }
        else{
            ++timeSinceAttack;
            List<Objective> objectivesToDeletion = new ArrayList<Objective>();
            //System.out.println("przed usu " + objectiveCount);
            for(Objective objective : objectives){//deleting defensive objectives
                if(timeSinceAttack > 6 || objective.getTargetedFlag().getTeam() == this){
                    objectivesToDeletion.add(objective);
                }
            }
            for(Objective objective : objectivesToDeletion){
                this.onPress( (int)objective.getX(), (int)objective.getY());
            }
            //System.out.println("po usu " + objectiveCount);

            if(shipCount > flagCount * 20 && objectiveCount == 0){
                timeSinceAttack = 0;
                //System.out.println("ATAKUJE druzyna " + color.r + " - " + color.g + " - " + color.b);
                List<Objective> objectivesCpy = new ArrayList<Objective>(objectives);
                for(Objective objective : objectivesCpy){//deleting all objectives
                    this.onPress( (int)objective.getX(), (int)objective.getY());
                }

                Flag newTargetedFlag = null;
                foundTarget:
                for(Flag myFlag : flags){
                    for(Flag flag2 : myFlag.getLinkedFlags()){
                        if(flag2.getTeam() != this){
                            newTargetedFlag = flag2;
                            if(flag2.getTeam() == gp.getNeutralTeam()){
                                break foundTarget;
                            }
                        }
                    }
                }
                if(newTargetedFlag != null){
                    this.onPress( (int)newTargetedFlag.getPos().x, (int)newTargetedFlag.getPos().y);
                }
            }
        }
    }

    private Boolean flagIsInObjectives(Flag flag){
        for(Objective objective : objectives){
            if(objective.getTargetedFlag() == flag) return true;
        }
        return false;
    }

    private void deleteObjectives(){
        for(Objective objective : objectives){
            this.onPress( (int)objective.getX(), (int)objective.getY());
        }
        offensiveObjectives.clear();
        defensiveObjectives.clear();
    }

    private void deleteOffensiveObjectives(){
        for(Objective objective : offensiveObjectives){
            this.onPress( (int)objective.getX(), (int)objective.getY());
        }
        offensiveObjectives.clear();
    }

    private void deleteDefensiveObjectives(){
        for(Objective objective : defensiveObjectives){
            this.onPress( (int)objective.getX(), (int)objective.getY());
        }
        defensiveObjectives.clear();
    }


}





