package fr.theskyblockman.quickmorph.gui;

import fr.theskyblockman.quickmorph.ActionableListener;
import fr.theskyblockman.quickmorph.QuickMorph;
import fr.theskyblockman.quickmorph.morphing.ClickAction;
import fr.theskyblockman.quickmorph.morphing.Morph;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class AskStopGUI implements ActionableListener {
    private boolean activated;
    public Morph morph;
    public Inventory inventory;
    public int secondsRemaining;
    public boolean isTimeout = false;
    public BukkitRunnable counter = new BukkitRunnable() {

        @Override
        public void run() {
            secondsRemaining--;

            if(secondsRemaining == 0) {
                morph.stop(true);
                isTimeout = false;
                this.cancel();
                morph.getPlayer().closeInventory();
            }

            resetInventoryContent();
        }
    };

    @Override
    public void setActivated(boolean value) {
        activated = value;
    }

    @Override
    public boolean isActivated() {
        return activated;
    }

    public AskStopGUI(Morph morph) {
        this.morph = morph;
        this.inventory = Bukkit.createInventory(null, 9, QuickMorph.currentLanguage.getString("cancel-morph"));
        resetInventoryContent();
    }

    public void resetInventoryContent() {
        if(isTimeout) {
            ItemStack timeRemaining = new ItemStack(Material.CLOCK);
            ItemMeta meta = timeRemaining.getItemMeta();
            assert meta != null;
            meta.setDisplayName(QuickMorph.currentLanguage.getString("time-left").replace("{0}", timeRemaining.toString()));

            timeRemaining.setItemMeta(meta);
            inventory.setItem(0, timeRemaining);
            inventory.setItem(2, timeRemaining);
            inventory.setItem(3, timeRemaining);
            inventory.setItem(5, timeRemaining);
            inventory.setItem(6, timeRemaining);
            inventory.setItem(8, timeRemaining);
        }

        inventory.setItem(1, morph.setClickAction(Material.RED_CONCRETE, ClickAction.REAPPEAR));
        inventory.setItem(4, morph.setClickAction(Material.ORANGE_CONCRETE, ClickAction.ENABLE_SOUL_MODE));
        inventory.setItem(7, morph.setClickAction(Material.GREEN_CONCRETE, ClickAction.CLOSE_GUI));
    }

    public void openInventory() {
        morph.activateListener(this);
        resetInventoryContent();
        morph.getPlayer().openInventory(this.inventory);
    }

    @EventHandler
    public void onGUIClosed(InventoryCloseEvent event) {
        if(event.getPlayer().getUniqueId() == morph.playerUUID && event.getInventory() == inventory) {
            morph.deactivateListener(this);
            morph.askingStop = false;
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack selectedItem = null;
        if(event.getCurrentItem() != null) selectedItem = event.getCurrentItem();
        else if(event.getCursor() != null) selectedItem = event.getCursor();

        if(selectedItem == null || !activated || event.getWhoClicked().getUniqueId() != this.morph.playerUUID) return;

        this.morph.runAction(selectedItem);
        this.morph.getPlayer().closeInventory();

        event.setCancelled(true);
    }

    public void setTimeout(int timeOut) {
        secondsRemaining = timeOut;
        isTimeout = true;

        counter.runTaskTimer(QuickMorph.instance, 1L, 20L);
    }
}