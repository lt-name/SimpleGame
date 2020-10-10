package xyz.champrin.simplegame;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.Task;
import xyz.champrin.simplegame.games.*;
import xyz.champrin.simplegame.games2.RedAlert;
import xyz.champrin.simplegame.games2.TrafficLight;
import xyz.champrin.simplegame.schedule.RoomSchedule;
import xyz.champrin.simplegame.schedule.RoomSchedule_2;

import java.util.*;

public class Room implements Listener {

    public SimpleGame plugin;
    public Games GameType;
    public Task GameTask;
    public String gameName;
    public String roomId;
    public LinkedHashMap<String, Object> data;
    public int game = 0;

    public Level level;
    public int xi, xa, yi, ya, zi, za;

    public LinkedHashMap<String, Integer> rank = new LinkedHashMap<>();
    public ArrayList<Player> waitPlayer = new ArrayList<>();
    public ArrayList<Player> gamePlayer = new ArrayList<>();
    public ArrayList<Player> viewPlayer = new ArrayList<>();
    public ArrayList<Player> finishPlayer = new ArrayList<>();
    public int joinGamePlayer = 0;
    public LinkedHashMap<String, String> playerNameTag = new LinkedHashMap<>();
    public Vector3 WaitPos, ViewPos, LeavePos;

    public ArrayList<String> BreakGame = new ArrayList<>(Arrays.asList("OreRace", "OreRace", "SnowballWar", "SnowballWar_2", "BeFast_1", "BeFast_2", "BeFast_4", "Weeding"));
    public ArrayList<String> PlaceGame = new ArrayList<>(Arrays.asList("BeFast_3", "KeepStanding_2", "SnowballWar"));
    public ArrayList<String> DamageGame = new ArrayList<>(Arrays.asList("KeepStanding", "KeepStanding_2", "SnowballWar", "FallingRun"));

    public HashMap<Player , Map<Integer, Item>> playerBagCache = new HashMap<>();

    public Room(String roomId, SimpleGame plugin) {
        this.plugin = plugin;
        this.roomId = roomId;
        this.data = plugin.roomInformation.get(roomId);

        this.level = plugin.getServer().getLevelByName((String) data.get("room_world"));
        this.gameName = (String) data.get("gameName");
        switch (gameName) {
            case "OreRace":
                this.GameType = new OreRace(this);
                GameTask = new RoomSchedule(this, GameType);
                break;
            case "OreRace_2":
                this.GameType = new OreRace_2(this);
                GameTask = new RoomSchedule(this, GameType);
                break;
            case "KeepStanding":
            case "KeepStanding_2":
                this.GameType = new KeepStanding(this);
                GameTask = new RoomSchedule(this, GameType);
                break;
            case "SnowballWar":
                this.GameType = new SnowballWar(this);
                GameTask = new RoomSchedule(this, GameType);
                break;
            case "SnowballWar_2":
                this.GameType = new SnowballWar_2(this);
                GameTask = new RoomSchedule(this, GameType);
                break;
            case "Parkour":
                this.GameType = new Parkour(this);
                GameTask = new RoomSchedule(this, GameType);
                break;
            case "MineRun":
                this.GameType = new MineRun(this);
                GameTask = new RoomSchedule(this, GameType);
                break;
            case "BeFast_1":
                this.GameType = new BeFast_1(this);
                GameTask = new RoomSchedule(this, GameType);
                break;
            case "BeFast_2":
                this.GameType = new BeFast_2(this);
                GameTask = new RoomSchedule(this, GameType);
                break;
            case "BeFast_3":
                this.GameType = new BeFast_3(this);
                GameTask = new RoomSchedule(this, GameType);
                break;
            case "BeFast_4":
                this.GameType = new BeFast_4(this);
                GameTask = new RoomSchedule(this, GameType);
                break;
            case "Weeding":
                this.GameType = new Weeding(this);
                GameTask = new RoomSchedule(this, GameType);
                break;
            case "TrafficLight":
                this.GameType = new TrafficLight(this);
                GameTask= new RoomSchedule_2(this, (xyz.champrin.simplegame.games2.Games) GameType);
                break;
            case "RedAlert":
                this.GameType = new RedAlert(this);
                GameTask= new RoomSchedule_2(this, (xyz.champrin.simplegame.games2.Games) GameType);
                break;

                /*case "MakeItem":
                this.GameType = new MakeItem(this);
               GameTask= new RoomSchedule(this, GameType);
                break;
            case "CollectOre":
                this.GameType = new CollectOre(this);
               GameTask= new RoomSchedule(this, GameType);
                break;
            case "CollectOre_2":
                this.GameType = new CollectOre_2(this);
               GameTask= new RoomSchedule(this, GameType);
                break;
            case "WatchingFeet":
                this.GameType = new WatchingFeet(this);
                GameTask = new RoomSchedule(this, GameType);
                break;

            case "FallingRun":
                this.GameType = new FallingRun(this);
                GameTask= new RoomSchedule_2(this, GameType);
                break;
            */
        }

        Server.getInstance().getScheduler().scheduleRepeatingTask(GameTask, 20);
        Server.getInstance().getPluginManager().registerEvents(this.GameType, this.plugin);

        String[] p1 = ((String) data.get("pos1")).split("\\+");
        String[] p2 = ((String) data.get("pos2")).split("\\+");
        this.xi = (Math.min(Integer.parseInt(p1[0]), Integer.parseInt(p2[0])));
        this.xa = (Math.max(Integer.parseInt(p1[0]), Integer.parseInt(p2[0])));
        this.yi = (Math.min(Integer.parseInt(p1[1]), Integer.parseInt(p2[1])));
        this.ya = (Math.max(Integer.parseInt(p1[1]), Integer.parseInt(p2[1])));
        this.zi = (Math.min(Integer.parseInt(p1[2]), Integer.parseInt(p2[2])));
        this.za = (Math.max(Integer.parseInt(p1[2]), Integer.parseInt(p2[2])));

        this.ViewPos = getVector3((String) data.get("view_pos"));
        this.WaitPos = getVector3((String) data.get("wait_pos"));
        this.LeavePos = getVector3((String) data.get("leave_pos"));
    }

    public Vector3 getVector3(String pos) {
        String[] p1 = pos.split("\\+");
        return new Vector3(Integer.parseInt(p1[0]), Integer.parseInt(p1[1]) + 2, Integer.parseInt(p1[2]));
    }

    /**
     * @param num 高度
     * @return 游戏区域内的随机坐标
     */
    public Vector3 getRandPos(int num) {
        int x = xi;
        int z = zi;
        int y = SimpleGame.RANDOM.nextInt(ya - yi + 1 + num) + yi;

        if (zi - za != 0) {
            z = SimpleGame.RANDOM.nextInt(za - zi + 1) + zi;
        }
        if (xi - xa != 0) {
            x = SimpleGame.RANDOM.nextInt(xa - xi + 1) + xi;
        }
        return new Vector3(x, y, z);
    }

    public void addPoint(Player player, int point) {
        String name = player.getName();
        rank.put(name, rank.get(name) + point);
    }

    public String getStatus() {
        if (game == 0) return "wait";
        if (game == 1) return "game";
        return null;
    }

    /**
     * @param p 玩家
     * @return 玩家当前状态
     */
    public String getPlayerMode(Player p) {
        if (waitPlayer.contains(p)) {
            return "wait";
        } else if (gamePlayer.contains(p)) {
            return "game";
        } else if (viewPlayer.contains(p)) {
            return "view";
        }
        return null;
    }

    public String getRank(String name) {
        int a = 0;
        for (Map.Entry<String, Integer> map : rank.entrySet()) {
            a = a + 1;
            if (map.getKey().equals(name)) {
                return "§a你的排名: §e[" + a + "] §a你的分数: §e[" + map.getValue() + "]";
            }
        }
        return "无数据";
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(PlayerChatEvent event) {
        Player p = event.getPlayer();
        if (getPlayerMode(p) != null) {
            event.setCancelled(true);
            arenaMsg(p.getNameTag() + " §e§l>§r " + event.getMessage());
        }
    }

    public ArrayList<Player> getAllPlayers() {
        ArrayList<Player> a = new ArrayList<>();
        a.addAll(gamePlayer);
        a.addAll(waitPlayer);
        a.addAll(viewPlayer);
        return a;
    }

    public int getMaxPlayers() {
        return (int) data.get("maxPlayers");
    }

    public int getMinPlayers() {
        return (int) data.get("minPlayers");
    }

    public int getLobbyPlayersNumber() {
        return waitPlayer.size();
    }

    public int getRemainPlayers() {
        return getMaxPlayers() - getLobbyPlayersNumber();
    }

    public void arenaMsg(String msg) {
        for (Player p : getAllPlayers()) {
            p.sendMessage(msg);
        }
    }

    public void arenaTiTle(String msg, String msg1) {
        for (Player p : getAllPlayers()) {
            p.sendTitle(msg, msg1, 2, 20 * 3, 2);
        }
    }

    public Map<String, Integer> sortByValue(Map<String, Integer> map) {

        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        //升序排序
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        map.clear();

        for (Map.Entry<String, Integer> s : list) {
            map.put(s.getKey(), s.getValue());
        }

        return map;
    }

    public void saveBag(Player gamePlayer) {
        this.playerBagCache.put(gamePlayer, gamePlayer.getInventory().getContents());
        gamePlayer.getInventory().clearAll();
        gamePlayer.getUIInventory().clearAll();
    }

    public void loadBag(Player gamePlayer) {
        gamePlayer.getInventory().clearAll();
        gamePlayer.getUIInventory().clearAll();
        gamePlayer.getInventory().setContents(this.playerBagCache.get(gamePlayer));
        this.playerBagCache.remove(gamePlayer);
    }

    public void setToView(Player player) {
        gamePlayer.remove(player);
        viewPlayer.add(player);

        player.setGamemode(3);
        player.setHealth(20);
        player.getFoodData().setLevel(20);
        player.getInventory().clearAll();
        player.removeAllEffects();
        player.getInventory().setItem(8, (Item.get(64, 0, 1)).setCustomName("§c退出游戏"));
        player.teleport(ViewPos);
        player.setSpawn(ViewPos);
        player.sendMessage(this.plugin.language.translateString("youEnterTheSpectatorMode"));
    }

    public void joinToRoom(Player p) {
        if (getPlayerMode(p) != null) {
            p.sendMessage(this.plugin.language.translateString("hasJoinedTheRoom"));
            return;
        } else if (waitPlayer.size() >= getMaxPlayers()) {
            p.sendMessage(this.plugin.language.translateString("roomIsFull"));
            return;
        } else if (game == 1) {
            p.sendMessage(this.plugin.language.translateString("theRoomHasStartedTheGame"));
            return;
        }
        Position v3 = Position.fromObject(WaitPos, plugin.getServer().getLevelByName((String) data.get("room_world")));
        saveBag(p);
        p.setGamemode(2);
        p.teleport(v3);

        this.waitPlayer.add(p);
        this.rank.put(p.getName(), 0);

        Item item = Item.get(64, 0, 1);
        item.setCustomName(this.plugin.language.translateString("item_QuitRoom_Name"));
        item.getNamedTag().putInt("SimpleGameItemType", 10);
        p.getInventory().setItem(8, item);

        arenaMsg(this.plugin.language.translateString("playerJoinRoom", p.getName()));
        p.sendMessage(this.plugin.language.translateString("exitRoomCommandPrompt"));
    }

    public void leaveRoom(Player p) {
        waitPlayer.remove(p);
        gamePlayer.remove(p);
        viewPlayer.remove(p);

        rank.remove(p.getName());

        Position v3 = Position.fromObject(LeavePos, plugin.getServer().getLevelByName((String) data.get("leave_pos")));
        p.teleport(v3);
        p.setSpawn(v3);
        p.removeAllEffects();
        this.joinGamePlayer = joinGamePlayer - 1;
        p.getInventory().clearAll();
        arenaMsg(this.plugin.language.translateString("playerQuitRoom", p.getName()));
    }

    public void checkTool() {
        switch (gameName) {
            case "OreRace":
            case "OreRace_2":
            case "BeFast_1":
            case "BeFast_2":
                for (Player player : gamePlayer) {
                    player.getInventory().setItem(0, Item.get(Item.DIAMOND_SHOVEL, 0, 1));
                    player.getInventory().setItem(1, Item.get(Item.DIAMOND_PICKAXE, 0, 1));
                    player.getInventory().setItem(2, Item.get(Item.DIAMOND_AXE, 0, 1));
                    player.getInventory().setItem(3, Item.get(Item.DIAMOND_SWORD, 0, 1));
                }
                break;
            case "SnowballWar":
            case "SnowballWar_2":
            case "Weeding":
                for (Player player : gamePlayer) {
                    player.getInventory().setItem(0, Item.get(Item.DIAMOND_SHOVEL, 0, 1));
                }
                break;
            case "BeFast_3":
                for (Player player : gamePlayer) {
                    player.getInventory().setItem(0, Item.get(Item.GLASS, 0, 64));
                    player.getInventory().setItem(1, Item.get(Item.GLASS, 0, 64));
                    player.getInventory().setItem(2, Item.get(Item.GLASS, 0, 64));
                }
                break;
            case "BeFast_4":
                for (Player player : gamePlayer) {
                    player.getInventory().setItem(0, Item.get(Item.STONE_PICKAXE, 0, 1));
                    player.getInventory().setItem(1, Item.get(Item.IRON_PICKAXE, 0, 1));
                    player.getInventory().setItem(2, Item.get(Item.GOLD_PICKAXE, 0, 1));
                    player.getInventory().setItem(3, Item.get(Item.DIAMOND_PICKAXE, 0, 1));
                }
                break;
            /*
            case "MakeItem":
            */
        }
    }

    public void startGame() {
        this.joinGamePlayer = waitPlayer.size();
        for (Player p : waitPlayer) {
            p.getInventory().clearAll();
            p.setGamemode(0);
            gamePlayer.add(p);
            playerNameTag.put(p.getName(), p.getNameTag());
            p.setNameTag("[PLAYER] §f" + p.getName());
        }
        waitPlayer.clear();
        checkTool();
        this.game = 1;
        this.plugin.freeRooms.remove(this);
    }


    public void stopGame() {
        getRichList();
        for (Player p : gamePlayer) {
            p.sendMessage(getRank(p.getName()));
        }
        unsetAllPlayers();
        this.game = 0;
        this.rank.clear();
        this.plugin.freeRooms.add(this);
    }

    public void getRichList() {
        int num = 0;
        this.rank = (LinkedHashMap<String, Integer>) sortByValue(rank);
        String r;
        for (Map.Entry<String, Integer> map : rank.entrySet()) {
            num = num + 1;
            if (num == 1) {
                r = "[1]";
            } else if (num == 2) {
                r = "[2]";
            } else if (num == 3) {
                r = "[3]";
            } else {
                r = "[" + num + "]";
            }
            for (Player p : gamePlayer) {
                p.sendMessage(r + " " + map.getKey() + " 分数:" + map.getValue());
            }
        }
    }

    public void serverStop() {
        for (Player p : gamePlayer) {
            p.setHealth(20);
            p.getFoodData().sendFoodLevel(20);
            p.getFoodData().sendFoodLevel();
            p.setGamemode(2);
            p.removeAllEffects();
            loadBag(p);
            p.setNameTag(playerNameTag.get(p.getName()));
        }
        for (Player p : viewPlayer) {
            p.setHealth(20);
            p.getFoodData().sendFoodLevel(20);
            p.getFoodData().sendFoodLevel();
            p.setGamemode(2);
            p.removeAllEffects();
            loadBag(p);
            p.setNameTag(playerNameTag.get(p.getName()));
        }
        for (Player p : waitPlayer) {
            p.setHealth(20);
            p.getFoodData().sendFoodLevel(20);
            p.getFoodData().sendFoodLevel();
            p.setGamemode(2);
            p.removeAllEffects();
            loadBag(p);
            p.setNameTag(playerNameTag.get(p.getName()));
        }
    }

    public void unsetAllPlayers() {

        for (Player p : gamePlayer) {
            Position v3 = Position.fromObject(LeavePos, plugin.getServer().getLevelByName((String) data.get("leave_pos")));
            p.teleport(v3);
            p.setSpawn(v3);
            p.setHealth(20);
            p.getFoodData().sendFoodLevel(20);
            p.getFoodData().sendFoodLevel();
            p.setGamemode(2);
            p.removeAllEffects();
            loadBag(p);
            p.setNameTag(playerNameTag.get(p.getName()));
        }
        this.gamePlayer.clear();
        this.waitPlayer.clear();
        this.viewPlayer.clear();
        this.joinGamePlayer = 0;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTouch(PlayerInteractEvent event) {
        if (getPlayerMode(event.getPlayer()) != null) {
            Player player = event.getPlayer();
            Item item = event.getItem();
            if (item != null && item.hasCompoundTag()) {
                if (item.getNamedTag().getInt("SimpleGameItemType") == 10) {
                    player.sendMessage(this.plugin.language.translateString("exitRoomPrompt"));
                    this.leaveRoom(player);
                }
            }
            event.setCancelled(true);
        }
    }

    /**
     * 玩家退出类事件
     **/
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        if (getPlayerMode(event.getPlayer()) != null) {
            this.leaveRoom(event.getPlayer());
        }
    }

    /**
     * 玩家受伤类事件
     **/
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        if (getPlayerMode(event.getEntity()) != null) {
            event.setCancelled(true);
        }
    }


    private void recoverPlayer(Player player, String[] p1) {
        Position v3;
        player.setHealth(20);
        player.getFoodData().sendFoodLevel(20);
        player.removeAllEffects();
        v3 = new Position(Integer.parseInt(p1[0]), Integer.parseInt(p1[1]), Integer.parseInt(p1[2]), level);
        player.teleport(v3);
        player.setSpawn(v3);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHit(EntityDamageEvent event) {
        Entity en = event.getEntity();
        if (en instanceof Player) {
            if (getPlayerMode((Player) en) != null) {
                if (getPlayerMode((Player) en).equals("game")) {
                    if (!DamageGame.contains(gameName)) {
                        event.setCancelled(true);
                        return;
                    }
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (getPlayerMode(event.getPlayer()) != null) {
            if (getPlayerMode(event.getPlayer()).equals("game")) {
                if (!BreakGame.contains(gameName)) {
                    event.setCancelled(true);
                    return;
                }
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (getPlayerMode(event.getPlayer()) != null) {
            if (getPlayerMode(event.getPlayer()).equals("game")) {
                if (!PlaceGame.contains(gameName)) {
                    event.setCancelled(true);
                    return;
                }
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {
        if (getPlayerMode(event.getPlayer()) != null) {
            if (!getPlayerMode(event.getPlayer()).equals("game")) {
                event.setCancelled(true);
            }
        }
    }


}
