package xyz.champrin.simplegame.games;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import xyz.champrin.simplegame.Room;

public class SnowballWar extends Games {

    public SnowballWar(Room room) {
        super(room);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        if (room.gameName.equals("SnowballWar")) {
            Player player = event.getPlayer();
            if (room.gamePlayer.contains(player)) {
                if (event.getBlock().getId() != Block.SNOW_BLOCK) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHit(EntityDamageEvent event) {
        if (room.gameName.equals("SnowballWar")) {
            Entity player = event.getEntity();
            if (player instanceof Player) {
                if (room.gamePlayer.contains(player)) {
                    if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                        room.setToView((Player) player);
                    } else {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}