package fr.theskyblockman.quickmorph;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayersListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        QuickMorph.assignPlayers();
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        QuickMorph.assignPlayers();
    }
}
