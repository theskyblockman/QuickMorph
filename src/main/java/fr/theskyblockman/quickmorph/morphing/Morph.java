package fr.theskyblockman.quickmorph.morphing;

import fr.drogonistudio.packets_listener.api.reflective.NmsReflection;
import fr.theskyblockman.quickmorph.ActionableListener;
import fr.theskyblockman.quickmorph.QuickMorph;
import fr.theskyblockman.quickmorph.gui.AskStopGUI;
import fr.theskyblockman.quickmorph.permission.MorphGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.pathfinder.Path;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Morph {
    public UUID playerUUID;
    public UUID entityUUID;
    public ItemStack[] initialInventory;
    public double initialHealth;
    public GameMode initialGameMode;
    public LootTable initialLootTable;
    public Collection<PotionEffect> initialEffects;
    public boolean created = false;
    public boolean freeView = false;
    public boolean freeMove = false;
    public boolean entityAI = true;
    public MorphingListener morphingListener;
    public SoulListener soulListener;
    public boolean soulMode = false;
    public AskStopGUI askingGUI;

    public LivingEntity getEntity() {
        return (LivingEntity) Bukkit.getEntity(entityUUID);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(playerUUID);
    }
    public boolean working;
    public boolean askingStop = false;

    public MorphGroup getGroup() {
        for (MorphGroup morphGroup : QuickMorph.morphGroups) {
            QuickMorph.associatedGroups.get(playerUUID);
            {
                return morphGroup;
            }
        }

        return null;
    }

    public void setEntityAI(boolean value) {
        getEntity().setAI(value);
        entityAI = value;
    }

    public Morph(Player player, EntityType entityType) {
        this(player, (LivingEntity) player.getWorld().spawnEntity(player.getLocation(), entityType));
    }


    public Morph(Player player, LivingEntity entity) {
        playerUUID = player.getUniqueId();
        entityUUID = entity.getUniqueId();

        morphingListener = new MorphingListener(this);
        soulListener = new SoulListener(this);
        askingGUI = new AskStopGUI(this);
    }

    public void clearPlayerEffects() {
        for(PotionEffect effect : getPlayer().getActivePotionEffects()) {
            getPlayer().removePotionEffect(effect.getType());
        }
    }

    public void setPlayerEffects(Collection<PotionEffect> effects) {
        clearPlayerEffects();
        getPlayer().addPotionEffects(effects);
    }

    public void create() {
        if(created) {
            return;
        }
        working = true;
        created = true;
        initialInventory = getPlayer().getInventory().getContents();
        initialHealth = getPlayer().getHealth();
        initialGameMode = getPlayer().getGameMode();
        initialLootTable = ((Lootable) getEntity()).getLootTable();
        initialEffects = getPlayer().getActivePotionEffects();
        clearPlayerEffects();

        resetStatusBar();

        QuickMorph.addPlayerToNoCollisionTeam(getPlayer());

        getNMSEntity().collides = false;

        getPlayer().setGameMode(GameMode.SURVIVAL);
        if(!getGroup().entityLoot) {
            ((Lootable) getEntity()).setLootTable(null);
        }
        if(!Objects.equals(getGroup().healthOwner, "entity")) {
            getEntity().setHealth(getPlayer().getHealth());
        } else {
            // sets the health of the player proportionally from the entity's health
            resetHealth(0);
        }

        getEntity().setInvulnerable(!getGroup().vulnerable);
        getPlayer().setInvulnerable(!getGroup().playerVulnerable);

        getPlayer().getInventory().clear();
        activateListener(morphingListener);

        resetInventory();

        removeDefaultPathfinding();

        new BukkitRunnable() {
            @Override
            public void run() {
                if(working) {
                    resetStatusBar();
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(QuickMorph.instance, 1L, 40L);
    }

    public void activateListener(ActionableListener listener) {
        QuickMorph.instance.getServer().getPluginManager().registerEvents(listener, QuickMorph.instance);
        listener.setActivated(true);
    }
    public void deactivateListener(ActionableListener listener) {
        HandlerList.unregisterAll(listener);
        listener.setActivated(false);
    }

    public ItemStack setClickAction(Material material, ClickAction action) {
        ItemStack stack = new ItemStack(material, 1);
        ItemMeta meta = Objects.requireNonNull(stack.getItemMeta());
        PersistentDataContainer container = meta.getPersistentDataContainer();

        container.set(new NamespacedKey(QuickMorph.instance, "action"), PersistentDataType.STRING, action.toID());

        meta.setDisplayName(ChatColor.RESET + action.displayName);

        stack.setItemMeta(meta);

        return stack;
    }

    public void resetInventory() {
        if(!created) {
            return;
        }
        if(!soulMode) {
            if(freeView) {
                getPlayer().getInventory().setItem(0, setClickAction(Material.DIAMOND, ClickAction.DISABLE_FREE_VIEW));
            } else {
                getPlayer().getInventory().setItem(0, setClickAction(Material.DIAMOND, ClickAction.ENABLE_FREE_VIEW));
            }
            if(entityAI) {
                getPlayer().getInventory().setItem(2, setClickAction(Material.GOLD_INGOT, ClickAction.DISABLE_FORCE_AI));
            } else {
                getPlayer().getInventory().setItem(2, setClickAction(Material.GOLD_INGOT, ClickAction.ENABLE_FORCE_AI));
            }
            if(freeMove) {
                getPlayer().getInventory().setItem(4, setClickAction(Material.IRON_INGOT, ClickAction.DISABLE_FREE_MOVE));
            } else {
                getPlayer().getInventory().setItem(4, setClickAction(Material.IRON_INGOT, ClickAction.ENABLE_FREE_MOVE));
            }
            getPlayer().getInventory().setItem(6, setClickAction(Material.STONE, ClickAction.UN_MORPH));
            getPlayer().getInventory().setItem(8, setClickAction(Material.NETHER_STAR, ClickAction.OPEN_MANUAL));
        } else {
            getPlayer().getInventory().setItem(4, setClickAction(Material.RED_CONCRETE, ClickAction.STOP_MORPH));
        }

        getPlayer().updateInventory();
    }

    public void resetStatusBar() {
        if(soulMode) {
            QuickMorph.sendActionBar(getPlayer(), ChatColor.GOLD + QuickMorph.currentLanguage.getString("right-click-to-capture"));
        } else if(created) {
            if(entityAI) {
                QuickMorph.sendActionBar(getPlayer(), ChatColor.RED + "\u2764 " + getEntity().getHealth() + "/" + Objects.requireNonNull(getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue() + "  " + ChatColor.AQUA + QuickMorph.currentLanguage.getString("left-click-to-pathfinder"));
            } else {
                QuickMorph.sendActionBar(getPlayer(), ChatColor.RED + "\u2764 " + getEntity().getHealth() + "/" + Objects.requireNonNull(getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue());
            }
        } else {
            QuickMorph.resetActionBar(getPlayer());
        }
    }

    public void stop(boolean restoreData) {
        if(!created) {
            return;
        }

        created = false;
        if(restoreData) {
            getPlayer().getInventory().setContents(initialInventory);
            setPlayerEffects(initialEffects);
        }
        getPlayer().setHealth(initialHealth);
        getPlayer().setGameMode(initialGameMode);
        getPlayer().setInvulnerable(false);

        getEntity().setInvulnerable(false);

        deactivateListener(morphingListener);
        deactivateListener(soulListener);

        QuickMorph.removePlayerToNoCollisionTeam(getPlayer());
        getNMSEntity().collides = true;
        working = false;
        resetStatusBar();
    }

    public void runAction(ItemStack itemStack) {
        try {

            PersistentDataContainer container = Objects.requireNonNull(itemStack.getItemMeta()).getPersistentDataContainer();
            String executionID;
            try {
                executionID = container.get(new NamespacedKey(QuickMorph.instance, "action"), PersistentDataType.STRING);
            } catch (Exception ignore) {
                return;
            }

            ClickAction.fromID(executionID).clickAction.run(this);

            resetInventory();
        } catch (NullPointerException ignore) { }
    }

    public void startSoulMode() {
        if(soulMode) return;

        deactivateListener(morphingListener);
        activateListener(soulListener);
        soulMode = true;
    }

    public void askStop() {
        freeMove = false;
        freeView = false;
        setEntityAI(false);
        askingStop = true;
        askingGUI.openInventory();
    }
    public net.minecraft.world.entity.LivingEntity getNMSEntity() {
        try {
            Object craftLivingEntity = NmsReflection.getCraftBukkitClass("entity.CraftLivingEntity").cast(getEntity());
            return (net.minecraft.world.entity.LivingEntity) craftLivingEntity.getClass().getDeclaredMethod("getHandle").invoke(craftLivingEntity);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    public void setEntityGoal(Location location) {
        PathfinderMob mob = (PathfinderMob) getNMSEntity();

        Path mobPath = mob.getNavigation().createPath(new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()), 1);

        mob.getNavigation().moveTo(mobPath, 1);
    }

    public void removeDefaultPathfinding() {
        PathfinderMob mob = (PathfinderMob) getNMSEntity();
        mob.goalSelector.setNewGoalRate(0);
    }


    public void askStop(int timeOut) {
        askStop();
        askingGUI.setTimeout(timeOut);
    }


    public void resetHealth(double damageTaken) {
        getPlayer().setHealth((getEntity().getHealth() / Objects.requireNonNull(getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue() - damageTaken) * 20);
    }

    public void teleportPosition(float x, float y, float z) {
        Set<ClientboundPlayerPositionPacket.RelativeArgument> set = EnumSet.noneOf(ClientboundPlayerPositionPacket.RelativeArgument.class);
        set.add(ClientboundPlayerPositionPacket.RelativeArgument.X_ROT);
        set.add(ClientboundPlayerPositionPacket.RelativeArgument.Y_ROT);
        ServerPlayer nmsPlayer = QuickMorph.getNMSServer().getPlayerList().getPlayer(getPlayer().getUniqueId());
        assert nmsPlayer != null;
        nmsPlayer.connection.teleport(x, y, z, 0, 0, set, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    public void updatePositions() {
        Location mixedLocations = getEntity().getLocation().clone();
        mixedLocations.setYaw(getPlayer().getLocation().getYaw());
        mixedLocations.setPitch(getPlayer().getLocation().getPitch());

        if(freeMove) {
            BlockIterator iterator = new BlockIterator(new Location(getPlayer().getWorld(), mixedLocations.getX(), mixedLocations.getY(), mixedLocations.getZ(), 90, 90), QuickMorph.configuration.getInt("raycast-limit"));

            Block groundBlock = null;

            while(iterator.hasNext()) {
                Block nextBlock = iterator.next();

                if(nextBlock.getType() != Material.AIR) {
                    groundBlock = nextBlock;
                    break;
                }
            }

            if(groundBlock == null) return;

            Location resultLoc = getPlayer().getLocation().clone();
            resultLoc.setY(groundBlock.getY() + 1);

            setEntityGoal(resultLoc);
        } else {
            teleportPosition((float) mixedLocations.getX(), (float) mixedLocations.getY(), (float) mixedLocations.getZ());
        }
    }
}
