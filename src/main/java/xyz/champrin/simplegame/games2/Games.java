package xyz.champrin.simplegame.games2;

import xyz.champrin.simplegame.Room;

public abstract class Games extends xyz.champrin.simplegame.games.Games {

    public int mainTime, startTime;

    public Games(Room room) {
        super(room);
    }

    public abstract void eachTick();
}
