package xyz.champrin.simplegame;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import xyz.champrin.simplegame.utils.Language;

import java.io.File;
import java.util.*;

public class SimpleGame extends PluginBase implements Listener {

    public static final Random RANDOM = new Random();

    public Config config,player;

    public Language language;

    public String COMMAND_NAME = "sgmap";
    public String PREFIX = "§a§l==> §c小游戏§a <==§r";
    public String GAME_NAME = "小游戏";

    public int GameMenuId = 100002;

    public LinkedHashMap<Integer, String> GameMap = new LinkedHashMap<>();

    public HashMap<String, Config> roomConfigCache = new HashMap<>();
    public LinkedHashMap<String, LinkedHashMap<String, Object>> roomInformation = new LinkedHashMap<>();//房间基本信息
    public LinkedHashMap<String, Room> rooms = new LinkedHashMap<>();//开启的房间信息
    public ArrayList<Room> freeRooms = new ArrayList<>();
    public HashMap<String, HashMap<String, String>> setters = new HashMap<>();
    public LinkedHashMap<Player, Room> gamePlayer = new LinkedHashMap<>(),
            waitPlayer = new LinkedHashMap<>(),
            viewPlayer = new LinkedHashMap<>();

    public int OwnPoint, TeamPoint, WinPoint;

    private static SimpleGame instance;

    public static SimpleGame getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;

        GameMap.put(1, "OreRace");
        GameMap.put(2, "BeFast_1");
        GameMap.put(3, "KeepStanding");
        GameMap.put(4, "KeepStanding_2");
        GameMap.put(5, "SnowballWar");
        GameMap.put(6, "SnowballWar_2");
        GameMap.put(7, "BeFast_2");
        GameMap.put(8, "Parkour");
        GameMap.put(9, "MineRun");
        GameMap.put(14,"BeFast_3");
        GameMap.put(15,"BeFast_4");
        GameMap.put(16,"Weeding");
        GameMap.put(18,"TrafficLight");
        GameMap.put(19,"RedAlert");

        /*TODO
        GameMap.put(10,"CollectOre");
        GameMap.put(11,"CollectOre_2");
        GameMap.put(12,"OreRace");
        GameMap.put(13,"WatchingFeet");
        GameMap.put(17,"FallingRun");
        GameMap.put(20,"MakeItem");
        */
    }

    public String getAllGameNameType() {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        for (Map.Entry<Integer, String> map : GameMap.entrySet()) {
            stringBuilder.append("[").append(map.getKey()).append("]").append(map.getValue()).append(",");
            i = i + 1;
            if (i == 3) {
                i = 0;
                stringBuilder.append("\n");
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public void onEnable() {
        long start = new Date().getTime();
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getLogger().info(PREFIX + "  §d加载中。。。§e| 此插件基于Champrin的SimpleGame插件开发");
        this.getLogger().info("本修改版的开源地址：https://github.com/lt-name/SimpleGame");
        this.loadConfig();
        //语言文件
        this.saveResource("Language/zh_CN.properties");
        String l = this.config.getString("language", "zh_CN");
        this.language = new Language(new Config(this.getDataFolder() + "/Language/" + l + ".properties", Config.PROPERTIES));
        this.loadRoomConfig();
        this.getLogger().info(PREFIX + "  §d已加载完毕。。。");
    }

    @Override
    public void onDisable() {
        //结束所有房间
        if (!this.rooms.isEmpty()) {
            for (Map.Entry<String, Room> map : rooms.entrySet()) {
                map.getValue().serverStop();
            }
        }
    }

    public void setRoomData(String roomId) {
        Room game = new Room(roomId, this);
        this.rooms.put(roomId, game);
        this.freeRooms.add(game);
        this.getServer().getPluginManager().registerEvents(game, this);
        if (!((Boolean) game.data.getOrDefault("Arena", false))) {
            game.GameType.madeArena();
        }
    }

    public String getGameName() {
        return this.GAME_NAME;
    }

    public Room getPlayerRoom(Player p) {
        for (Map.Entry<String, Room> map : this.rooms.entrySet()) {
            Room room = map.getValue();
            if (room.gamePlayer.contains(p) || room.waitPlayer.contains(p) || room.viewPlayer.contains(p)) {
                return room;
            }
        }
        return null;
    }

    //判断房间是否存在
    public boolean isRoomSet(String roomName) {
        return this.rooms.containsKey(roomName);
    }

    public Room getRoom(String roomName) {
        return this.rooms.getOrDefault(roomName, null);
    }

    public int getAllPlayerCount() {
        return this.gamePlayer.size() + this.waitPlayer.size() + this.viewPlayer.size();
    }

    public int getAllRoomCount() {
        return this.rooms.size();
    }

    public int getAllFreeRoomCount() {
        return this.freeRooms.size();
    }

    public String getGAME_NAME() {
        return this.GAME_NAME;
    }


    public void loadConfig() {
        this.getLogger().info("-配置文件加载中...");
        this.saveResource("config.yml", false);
        this.config = new Config(this.getDataFolder() + "/config.yml", Config.YAML);
        this.OwnPoint = this.config.getInt("占领加分", 10);
        this.TeamPoint = this.config.getInt("团队加分", 2);
        this.WinPoint = this.config.getInt("最终胜利加分", 10);
        this.player = new Config(this.getDataFolder() + "/player.yml", Config.YAML);
        File file = new File(this.getDataFolder() + "/Room/");
        if (!file.exists() && !file.mkdirs()) {
            this.getServer().getLogger().info("文件夹创建失败");
        }
    }

    public void loadRoomConfig() {
        this.getLogger().info("-房间信息加载中...");
        File[] files = new File(this.getDataFolder() + "/Room/").listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    Config room = new Config(file, Config.YAML);
                    String FileName = file.getName().substring(0, file.getName().lastIndexOf("."));
                    this.roomInformation.put(FileName, new LinkedHashMap<>(room.getAll()));
                    if (room.getBoolean("state", false)) {
                        this.setRoomData(FileName);
                        this.getLogger().info("   房间§b" + FileName + "§r加载完成");
                    }
                }
            }
        }
        this.getLogger().info("-房间信息加载完毕...");
    }


    public Config getRoomConfig(String roomName) {
        if (!this.roomConfigCache.containsKey(roomName)) {
            this.roomConfigCache.put(roomName,
                    new Config(this.getDataFolder() + "/Room/" + roomName + ".yml", Config.YAML));
        }
        return this.roomConfigCache.get(roomName);
    }

    //判断房间是否存在
    public boolean roomExist(String roomName) {
        return this.roomInformation.containsKey(roomName);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.setters.remove(event.getPlayer().getName());
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player p = event.getPlayer();
        if (getPlayerRoom(p) == null) return;
        if (event.getMessage().contains("@hub")) {
            event.setCancelled(true);
            p.sendMessage("§c>  §f你已退出游戏！");
            this.getPlayerRoom(p).leaveRoom(p);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        String name = p.getName();
        if (this.setters.containsKey(name)) {
            event.setCancelled(true);
            Block b = event.getBlock();

            String room_name = this.setters.get(name).get("room_name");
            Config room = this.getRoomConfig(room_name);

            String xyz = Math.round(Math.floor(b.x)) + "+" + Math.round(Math.floor(b.y)) + "+" + Math.round(Math.floor(b.z));

            int step = Integer.parseInt(this.setters.get(name).get("step"));

            switch (step) {
                case 1:
                    room.set("wait_pos", xyz);//等待点
                    this.setters.get(name).put("step", step + 1 + "");
                    p.sendMessage(this.language.translateString("setTheGameAreaPoint", 1));
                    break;
                case 2:
                    room.set("pos1", xyz);
                    this.setters.get(name).put("step", step + 1 + "");
                    p.sendMessage(this.language.translateString("setTheGameAreaPoint", 2));
                    break;
                case 3:
                    room.set("pos2", xyz);
                    room.set("room_world", b.level.getFolderName());
                    this.setters.get(name).put("step", step + 1 + "");
                    p.sendMessage(this.language.translateString("setViewingPoints"));
                    break;
                case 4:
                    room.set("view_pos", xyz);
                    this.setters.get(name).put("step", step + 1 + "");
                    p.sendMessage(this.language.translateString("setQuitPoint"));
                    break;
                case 5:
                    room.set("leave_pos", xyz);
                    room.set("leave_world", b.level.getFolderName());
                    room.set("state", true);
                    this.roomInformation.put(room_name, (LinkedHashMap<String, Object>) room.getAll());
                    this.setRoomData(room_name);
                    this.setters.remove(name);
                    p.sendMessage(this.language.translateString("roomSetupIsComplete"));
                    break;
            }
            room.save();
        }
    }

    public void Op_CHelpMessage(CommandSender sender) {
        sender.sendMessage(">  /" + COMMAND_NAME + " add [房间名] [游戏类型] ------ §d创建新房间");
        sender.sendMessage(">  /" + COMMAND_NAME + " set [房间名] ------ §d设置房间");
        sender.sendMessage(">  /" + COMMAND_NAME + " del [房间名] ------ §d删除房间");
        sender.sendMessage(">  游戏类型: " + getAllGameNameType());
    }

    public void CHelpMessage(CommandSender sender) {
        sender.sendMessage(">  §f==========" + PREFIX + "§f==========§r");
        sender.sendMessage(">  /" + COMMAND_NAME + " join ------ §d加入游戏");
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (COMMAND_NAME.equals(command.getName())) {
            if (args.length < 1) {
                this.CHelpMessage(sender);
                if (sender.isOp()) {
                    this.Op_CHelpMessage(sender);
                }
            } else {
                switch (args[0]) {
                    case "join":
                        if (sender instanceof Player) {
                            sendMenu((Player) sender);
                        }
                        break;
                    case "set":
                        if (sender instanceof Player) {
                            if (args.length < 2) {
                                sender.sendMessage(this.language.translateString("insufficientParameters"));
                                break;
                            }
                            if (!this.roomExist(args[1])) {
                                sender.sendMessage(this.language.translateString("roomDoesNotExist"));
                                break;
                            }
                            if (this.isRoomSet(args[1])) {
                                Room a = this.rooms.get(args[1]);
                                if (a.game != 0 || a.gamePlayer != null) {
                                    sender.sendMessage(this.language.translateString("theRoomIsPlaying"));
                                    break;
                                }
                            }
                            HashMap<String, String> list = new HashMap<>();
                            list.put("room_name", args[1]);
                            list.put("step", 1 + "");
                            setters.put(sender.getName(), list);
                            sender.sendMessage(this.language.translateString("setWaitingPoint", args[1]));
                        } else {
                            sender.sendMessage(this.language.translateString("pleaseUseInTheGame"));
                        }
                        break;
                    case "add":
                        if (args.length < 3) {
                            sender.sendMessage(this.language.translateString("insufficientParameters"));
                            break;
                        }
                        if (this.roomExist(args[1])) {
                            sender.sendMessage(this.language.translateString("roomAlreadyExists"));
                            break;
                        }
                        Config a = this.getRoomConfig(args[1]);
                        a.set("state", false);
                        a.set("room_world", null);
                        a.set("gameTime", 120);
                        a.set("startTime", 30);
                        a.set("maxPlayers", 5);
                        a.set("minPlayers", 5);
                        a.set("gameName", GameMap.get(Integer.parseInt(args[2])));
                        a.save();
                        roomInformation.put(args[1], (LinkedHashMap<String, Object>) a.getAll());
                        sender.sendMessage(this.language.translateString("roomCreatedSuccessfully", args[1]));
                        this.getServer().dispatchCommand(sender, "sgmap set " + args[1]);
                        break;
                    case "del":
                        if (args.length < 2) {
                            sender.sendMessage(this.language.translateString("insufficientParameters"));
                            break;
                        }
                        if (!this.roomExist(args[1])) {
                            sender.sendMessage(this.language.translateString("roomDoesNotExist"));
                            break;
                        }
                        boolean file = new File(this.getDataFolder() + "/Room/" + args[1] + ".yml").delete();
                        if (file) {
                            if (rooms.containsKey(args[1])) {
                                rooms.get(args[1]).stopGame();
                                rooms.remove(args[1]);
                            }
                            this.setters.remove(sender.getName());
                            roomInformation.remove(args[1]);
                            sender.sendMessage(this.language.translateString("roomDeletedSuccessfully"));
                        } else {
                            sender.sendMessage(this.language.translateString("roomDeletionFailed"));
                        }
                        break;
                    case "help":
                    default:
                        this.CHelpMessage(sender);
                        if (sender.isOp()) {
                            this.Op_CHelpMessage(sender);
                        }
                        break;
                }
            }
        }
        return true;
    }

    public void sendMenu(Player player) {
        FormWindowSimple form = new FormWindowSimple(
                this.language.translateString("GUI_Join_Title"),
                this.language.translateString("GUI_Join_Text"));
        form.addButton(new ElementButton(this.language.translateString("GUI_Join_Button_RandomJoin"),
                new ElementButtonImageData("path", "textures/blocks/bedrock.png")));
        for (Room room : rooms.values()) {
            String state = this.language.translateString("GUI_Join_Button_Room", room.roomId, room.gameName) + "\n";
            if (room.game == 0) {
                state += this.language.translateString("GUI_Join_Button_Room_Wait", room.getLobbyPlayersNumber(), room.getMaxPlayers());
            } else {
                state += this.language.translateString("GUI_Join_Button_Room_Game");
            }
            form.addButton(new ElementButton(state));
        }
        player.showFormWindow(form, GameMenuId);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFormResponse(PlayerFormRespondedEvent event) {
        Player p = event.getPlayer();
        if (GameMenuId == event.getFormID()) {
            FormResponseSimple response = (FormResponseSimple) event.getResponse();
            if (response == null) return;
            int clickedButtonId = response.getClickedButtonId();
            if (clickedButtonId != 0) {
                Room room = getRoomByIndex(clickedButtonId);
                if (room != null) {
                    room.joinToRoom(p);
                }
            } else {
                if (this.rooms.size() <= 0) {
                    p.sendMessage(this.language.translateString("noRoom"));
                    return;
                } else if (this.freeRooms.size() <= 0) {
                    p.sendMessage(this.language.translateString("noFreeRoom"));
                    return;
                }
                int a = new Random().nextInt(freeRooms.size());
                Room room = freeRooms.get(a);
                if (room != null) {
                    room.joinToRoom(p);
                }
            }
        }
    }

    public Room getRoomByIndex(int index) {
        int i = 1;
        for (Room room : rooms.values()) {
            if (i == index) {
                return room;
            }
            i++;
        }
        return null;
    }

}
