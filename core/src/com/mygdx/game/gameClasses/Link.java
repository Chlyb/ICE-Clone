package com.mygdx.game.gameClasses;


import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Link implements Serializable {
    transient public final GamePacket gp;
    public final Flag flag1;
    public final Flag flag2;

    public Link(GamePacket gp, Flag flag1, Flag flag2) {
        this.gp = gp;
        this.flag1 = flag1;
        this.flag2 = flag2;
        if(!dupilcated()){
            flag1.addLinkedFlag(flag2);
            flag2.addLinkedFlag(flag1);
            gp.addLink(this);
        }
    }
    Link(Flag flag1, Flag flag2){
        this.gp = null;
        this.flag1 = flag1;
        this.flag2 = flag2;
    }

    public Boolean dupilcated(){
        for(Link link : gp.getLinks()){
            if( (link.flag1 == flag1 && link.flag2 == flag2) || (link.flag1 == flag2 && link.flag2 == flag1)) return true;
        }
        return false;
    }
}
