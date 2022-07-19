package fr.theskyblockman.quickmorph;

import fr.drogonistudio.packets_listener.SimplePacketListenerPlugin;
import fr.drogonistudio.packets_listener.api.reflective.NmsReflection;
import fr.theskyblockman.quickmorph.commands.CommandConfig;
import fr.theskyblockman.quickmorph.commands.CommandMorph;
import fr.theskyblockman.quickmorph.commands.CommandTest;
import fr.theskyblockman.quickmorph.permission.GroupType;
import fr.theskyblockman.quickmorph.permission.MorphGroup;
import fr.theskyblockman.quickmorph.translation.Language;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class QuickMorph extends JavaPlugin {
    public static YamlConfiguration configuration;
    public static List<MorphGroup> morphGroups = new ArrayList<>();
    public static boolean isConfigEdited;
    public static QuickMorph instance;
    public static Language currentLanguage;
    public static Map<UUID, Integer> associatedGroups = new HashMap<>();
    public static PlayerTeam noCollisionTeam;
    public static SimplePacketListenerPlugin listenerPlugin;
    public static void assignPlayers() {
        List<Integer> mentionedIntegers = new ArrayList<>();
        List<MorphGroup> registeredGroups = new ArrayList<>();
        int doubleAmount = 0;
        for (MorphGroup morphGroup : morphGroups) {
            if(mentionedIntegers.contains(morphGroup.priority)) {
                doubleAmount++;
            } else {
                mentionedIntegers.add(morphGroup.priority);
                registeredGroups.add(morphGroup);
            }
        }
        if (doubleAmount > 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if(player.isOp()) {
                    player.sendMessage(ChatColor.RED + currentLanguage.getString("priority-double").replace("{0}", Integer.toString(doubleAmount)));
                }
                int setPriority = -1;
                MorphGroup thoughtWinner = null;
                for (MorphGroup morphGroup : registeredGroups) {
                    if(morphGroup.priority > setPriority) {
                        switch (morphGroup.groupType) {
                            case OP -> {
                                if(player.isOp()) {
                                    setPriority = morphGroup.priority;
                                    thoughtWinner = morphGroup;
                                }
                            }
                            case CUSTOM -> {
                                if(player.hasPermission(morphGroup.groupName)) {
                                    setPriority = morphGroup.priority;
                                    thoughtWinner = morphGroup;
                                }
                            }
                            case DEFAULT -> {
                                setPriority = morphGroup.priority;
                                thoughtWinner = morphGroup;
                            }
                        }
                    }
                }

                if(thoughtWinner == null) {
                    return;
                }

                associatedGroups.put(player.getUniqueId(), thoughtWinner.priority);
            }
        }
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        listenerPlugin.onEnable();

        configuration = (YamlConfiguration) getConfig();
        morphGroups.add(new MorphGroup(Objects.requireNonNull(configuration.getConfigurationSection("default")), GroupType.DEFAULT));
        morphGroups.add(new MorphGroup(Objects.requireNonNull(configuration.getConfigurationSection("op")), GroupType.OP));
        for(String key : Objects.requireNonNull(configuration.getConfigurationSection("custom")).getValues(false).keySet()) {
            morphGroups.add(new MorphGroup(Objects.requireNonNull(configuration.getConfigurationSection("custom." + key )), GroupType.CUSTOM));
        }
        Objects.requireNonNull(getCommand("configuration")).setExecutor(new CommandConfig());
        Objects.requireNonNull(getCommand("morph")).setExecutor(new CommandMorph());
        Objects.requireNonNull(getCommand("test")).setExecutor(new CommandTest());
        currentLanguage = new Language(configuration.getString("language"));

        net.minecraft.world.scores.Scoreboard scoreboard = getNMSServer().getScoreboard();
        if(scoreboard.getPlayerTeam(new NamespacedKey(QuickMorph.instance, "noCollision").toString()) == null) {
            noCollisionTeam = scoreboard.addPlayerTeam(new NamespacedKey(QuickMorph.instance, "noCollision").toString());
        } else {
            noCollisionTeam = scoreboard.getPlayerTeam(new NamespacedKey(QuickMorph.instance, "noCollision").toString());
        }
        assert noCollisionTeam != null;
        noCollisionTeam.setCollisionRule(Team.CollisionRule.PUSH_OTHER_TEAMS);
        assignPlayers();
    }

    @Override
    public void onLoad() {
        listenerPlugin = new SimplePacketListenerPlugin(getServer(), getLogger(), this);

        listenerPlugin.onLoad();
        saveDefaultConfig();
        instance = this;
    }

    @Override
    public void onDisable() {
        listenerPlugin.onDisable();

        if(isConfigEdited) {
            for(MorphGroup morphGroup : morphGroups) {
                if(morphGroup.groupType != GroupType.CUSTOM) {
                    getConfig().set(morphGroup.groupName, morphGroup.rawSection);
                } else {
                    getConfig().set("custom." + morphGroup.groupName.replace(".", "-"), morphGroup.rawSection);
                }
            }
            saveConfig();
        }
    }

    public static MinecraftServer getNMSServer() {
        try {
            Object craftServer = NmsReflection.getCraftBukkitClass("CraftServer").cast(QuickMorph.instance.getServer());

            return (MinecraftServer) craftServer.getClass().getDeclaredMethod("getServer").invoke(craftServer);
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            // sry for deprecated but there isn't any javadocs, so I don't know what else to do to remove the deprecation
            //noinspection deprecation
            return MinecraftServer.getServer();
        }
    }

    public static void addPlayerToNoCollisionTeam(Player player) {
        getNMSServer().getScoreboard().addPlayerToTeam(player.getName(), noCollisionTeam);
    }

    public static void removePlayerToNoCollisionTeam(Player player) {
        try {
            getNMSServer().getScoreboard().removePlayerFromTeam(player.getName(), noCollisionTeam);
        } catch (IllegalStateException ignore) { }
    }

    public static void sendActionBar(Player player, String text) {
        ServerPlayer nmsPlayer = getNMSServer().getPlayerList().getPlayer(player.getUniqueId());
        ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(ComponentUtils.fromMessage(() -> text));
        assert nmsPlayer != null;

        nmsPlayer.connection.send(packet);
    }

    public static void resetActionBar(Player player) {
        sendActionBar(player, "");
    }
}
