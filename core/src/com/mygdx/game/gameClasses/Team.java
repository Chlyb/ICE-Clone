package com.mygdx.game.gameClasses;

import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Team implements Serializable {
    public final GamePacket gp;
    //public final Color color;
    //public final Color darkColor;
    public final int color;
    //public final int darkColor;

    private int speed;
    private int health;
    private int damage;
    private List<Objective> objectives;
    private List<Ship> ships;
    private List<Flag> flags;
    private List<Flag> linkedFlags;
    public int objectiveCount;
    private int shipCount;
    private int flagCount;
    private int ix;

    private int iteAI;
    private int timeSinceAttack;

    //AI
    private List<Objective> offensiveObjectives;
    private List<Objective> defensiveObjectives;

    public Team(GamePacket gp, int color) {
        this.gp = gp;
        this.color = color;
        //this.darkColor = new Color(color.r/3,color.g/3,color.b/3,1);

        speed = 5;
        health = 5;
        damage = 5;
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

    public void onPress(int x, int y) {
        Boolean deleted = false;
        for (Objective objective : objectives) {
            //System.out.println(objective.getX() + " - " + objective.getY() );
            //System.out.println(x + " - " + y);

            if (Math.pow((objective.getX() - x), 2) + Math.pow((objective.getY() - y), 2) <= Math.pow(objective.RADIUS, 2)) {
                objective.delete();
                deleted = true;
                break;
            }
        }
        if (!deleted) {
            //System.out.println("NIIIiiiiii");
            Vector2 v = new Vector2(x, y);
            //System.out.println(v.x + "-" + v.y);
            new Objective(this, v);
            //objectives.add(new Objective(this, new Vector2(x, y))
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

    public int getHealth(){
        return health;
    }
    public int getDamage(){return damage;}
    public void setHealth(int h){health = h;}
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
                    //new Team(gp, new Color(0,1,0,1));
                    new Team(gp, 1);
                    existingTeams.add("Green");
                    ++currentenemyCount;
                }
            }
            else if(x == 1){
                if(!existingTeams.contains("Blue")){
                    //new Team(gp, new Color(0,1,1,1));
                    new Team(gp, 2);
                    existingTeams.add("Blue");
                    ++currentenemyCount;
                }
            }
            else if(x == 2){
                if(!existingTeams.contains("Purple")){
                    //new Team(gp, new Color(0.5f,0,0.5f,1));
                    new Team(gp, 3);
                    existingTeams.add("Purple");
                    ++currentenemyCount;
                }
            }
            else if(x == 3){
                if(!existingTeams.contains("Orange")){
                    //new Team(gp, new Color(1,0.3f,0,1));
                    new Team(gp, 4);
                    existingTeams.add("Orange");
                    ++currentenemyCount;
                }
            }
            else if(x == 4){
                if(!existingTeams.contains("Red")){
                    //new Team(gp, new Color(1,0,0,1));
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
                        //this.onPress( (int)objective.getX(), (int)objective.getY());
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





