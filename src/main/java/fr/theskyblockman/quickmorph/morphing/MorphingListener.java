package fr.theskyblockman.quickmorph.morphing;

import fr.drogonistudio.packets_listener.api.event.PacketSendEvent;
import fr.theskyblockman.quickmorph.ActionableListener;
import fr.theskyblockman.quickmorph.QuickMorph;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;

import java.util.Objects;

public class MorphingListener implements ActionableListener {
    public Morph morph;

    public MorphingListener(Morph morph) {
        this.morph = morph;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if(!activated) return;
        if(event.getPlayer().getUniqueId() != this.morph.playerUUID) return;
        if(event.getInventory() == morph.askingGUI.inventory) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.RED + QuickMorph.currentLanguage.getString("cannot-open-inventories"));
    }

    @EventHandler
    public void onHandClick(PlayerInteractEvent event) {
        if(!activated) return;
        if(event.getPlayer().getUniqueId() != this.morph.playerUUID) return;
        if(event.getItem() != null && event.getItem().getType() != Material.AIR && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            this.morph.runAction(event.getItem());
            this.morph.resetStatusBar();
        } else if(event.getAction() == Action.LEFT_CLICK_AIR) {
            BlockIterator blockIterator = new BlockIterator(event.getPlayer(), QuickMorph.configuration.getInt("raycast-limit"));
            Block found = null;
            while(blockIterator.hasNext()) {
                Block rayCastedBlock = blockIterator.next();
                if(rayCastedBlock.getType() != Material.AIR) {
                    found = rayCastedBlock;
                    break;
                }
            }

            if(found == null) return;

            if(found.getLocation().add(0, 1, 0).getBlock().getType() != Material.AIR) return;

            morph.setEntityGoal(found.getLocation().add(0, 1, 0));
        } else if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
            morph.setEntityGoal(Objects.requireNonNull(event.getClickedBlock()).getLocation());
        }
        event.setCancelled(true);
    }
    @EventHandler
    public void onPlayerPickup(EntityPickupItemEvent event) {
        if(!activated) return;
        if(!(event.getEntity() instanceof Player player)) return;

        if(player.getUniqueId() == this.morph.playerUUID) event.setCancelled(true);
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if(!activated) return;
        if(event.getPlayer().getUniqueId() != this.morph.playerUUID) return;

        Location from = event.getFrom().clone();
        Location to = Objects.requireNonNull(event.getTo()).clone();
        Location result = from.subtract(to);

        boolean isRotationChanged = result.getYaw() != 0 || result.getPitch() != 0;
        boolean isPositionChanged = result.getX() != 0 || result.getY() != 0 || result.getZ() != 0;

        if(isPositionChanged) {
            if (morph.freeMove) {
                Location rayCastLocation = to.clone();
                rayCastLocation.setYaw(90);
                rayCastLocation.setPitch(90);

                BlockIterator blockIterator = new BlockIterator(rayCastLocation, 0, QuickMorph.configuration.getInt("raycast-limit"));
                Block found = null;
                while (blockIterator.hasNext()) {
                    Block iteratedBlock = blockIterator.next();
                    if(iteratedBlock.getType() != Material.AIR) {
                        found = iteratedBlock;
                        break;
                    }
                }

                if(to.distance(morph.getEntity().getLocation()) > morph.getGroup().maxEntityDistance) {
                    morph.getPlayer().sendMessage(ChatColor.RED + QuickMorph.currentLanguage.getString("too-far"));
                    if(found == null) {
                        event.setCancelled(true);
                        return;
                    }
                    Location downLocation = found.getLocation();
                    downLocation.add(0, 2, 0);
                    downLocation.setYaw(morph.getPlayer().getLocation().getYaw());
                    downLocation.setPitch(morph.getPlayer().getLocation().getPitch());
                    downLocation.setX(morph.getPlayer().getLocation().getX());
                    downLocation.setZ(morph.getPlayer().getLocation().getZ());
                    event.setTo(downLocation);
                }

                if(found == null) return;

                morph.setEntityGoal(found.getLocation().add(0, 1, 0));
            }
        }

        if(isRotationChanged && !morph.freeView) {
            Location editedLocation = morph.getEntity().getLocation().clone();
            editedLocation.setYaw(to.getYaw());
            editedLocation.setPitch(to.getPitch());

            morph.getEntity().teleport(editedLocation);
        }

    }
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if(!activated) return;

        if(event.getEntity().getUniqueId() == morph.playerUUID) {
            morph.stop(false);
            morph.deactivateListener(this);
        } else if(event.getEntity().getUniqueId() == morph.entityUUID) {
            morph.askStop(5);
            morph.deactivateListener(this);
        }
    }

    @EventHandler
    public void onPacketOut(PacketSendEvent event) {
        if(!activated) return;
        if(event.getPacket() instanceof ClientboundMoveEntityPacket packet && !morph.askingStop) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    morph.updatePositions();
                }
            }.runTask(QuickMorph.instance);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if(!activated) return;

        if(event.getPlayer().getUniqueId() == morph.getPlayer().getUniqueId()) {
            event.setCancelled(true);
            morph.resetInventory();
        }
    }

    private boolean activated;

    @Override
    public void setActivated(boolean value) {
        activated = value;
    }

    @Override
    public boolean isActivated() {
        return activated;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(!activated) return;
        if(event.getEntity().getUniqueId() == morph.getPlayer().getUniqueId()) {
            event.setCancelled(true);
        } else if(event.getEntity().getUniqueId() == morph.getEntity().getUniqueId()) {
            morph.resetStatusBar();
            morph.resetHealth(event.getFinalDamage());
        }
    }

    @EventHandler
    public void onPlayerRegenerate(EntityRegainHealthEvent event) {
        if(!activated) return;
        if(event.getEntity().getUniqueId() == morph.getPlayer().getUniqueId()) {
            event.setCancelled(true);
        } else if(event.getEntity().getUniqueId() == morph.getEntity().getUniqueId()) {
            morph.resetStatusBar();
            morph.resetHealth(event.getAmount() * -1);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if(event.getEntity().getUniqueId() != morph.getEntity().getUniqueId()) return;
        if(!activated) return;

        event.setCancelled(true);
    }

}
