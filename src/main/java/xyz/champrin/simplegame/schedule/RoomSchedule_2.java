package xyz.champrin.simplegame.schedule;


import cn.nukkit.Player;
import cn.nukkit.scheduler.Task;
import xyz.champrin.simplegame.Room;
import xyz.champrin.simplegame.SimpleGame;
import xyz.champrin.simplegame.games2.Games;


public class RoomSchedule_2 extends Task {

    private int maxTime, StartTime;
    private Room room;
    private Games game;

    public RoomSchedule_2(Room room, Games game) {
        this.room = room;
        this.StartTime = (int) room.data.get("game.startTime");
        game.startTime = StartTime;
        this.maxTime = (int) room.data.get("gameTime");
        game.mainTime = maxTime;
        this.game = game;
    }

    @Override
    public void onRun(int tick) {
        if (room.game == 0) {
            if (game.mainTime != maxTime) {
                this.game.mainTime = maxTime;
            }
            if (room.waitPlayer.size() < room.getMinPlayers()) {
                this.game.startTime = StartTime;
                for (Player p : room.waitPlayer) {
                    p.sendPopup(SimpleGame.getInstance().language.translateString("wait_Bottom"));
                }
            } else if (room.waitPlayer.size() >= room.getMinPlayers()) {
                this.game.startTime = game.startTime - 1;
                if (room.waitPlayer.size() >= room.getMaxPlayers() - room.getMinPlayers()) {
                    this.game.startTime = 20;
                } else if (room.waitPlayer.size() >= room.getMaxPlayers()) {
                    this.game.startTime = 10;
                }
                for (Player p : room.waitPlayer) {
                    p.sendPopup(Countdown.countDown(game.startTime));
                }
                if (this.game.startTime <= 0) {
                    if (room.waitPlayer.size() >= room.getMinPlayers()) {
                        room.startGame();
                    }
                    this.game.startTime = StartTime;
                }
            }
        }

        if (room.game == 1) {
            this.game.mainTime = game.mainTime - 1;
            game.eachTick();
            for (Player p : room.gamePlayer) {
                p.sendTip(SimpleGame.getInstance().language.translateString("game_Bottom_2", room.gamePlayer.size(), game.mainTime));
            }
            if (room.gamePlayer.size() <= 1 || game.mainTime < 0) {
                this.game.mainTime = maxTime;
                room.stopGame();
                game.madeArena();
            }
        }

    }


}