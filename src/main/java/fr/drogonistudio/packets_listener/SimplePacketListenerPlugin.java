package fr.drogonistudio.packets_listener;

import fr.drogonistudio.packets_listener.api.reflective.NmsReflection;
import fr.drogonistudio.packets_listener.reflective.InjectionUtils;
import fr.theskyblockman.quickmorph.QuickMorph;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Core's main class.
 * 
 * @author DrogoniEntity
 */
public class SimplePacketListenerPlugin
{
    private final Server server;

    private final Logger logger;
    private final JavaPlugin basePlugin;

    public Logger getLogger() {
        return logger;
    }
    public static SimplePacketListenerPlugin pluginInstance = null;

    public SimplePacketListenerPlugin(Server server, Logger logger, JavaPlugin basePlugin) {
        this.server = server;
        this.logger = logger;
        this.basePlugin = basePlugin;

        if(pluginInstance != null) pluginInstance = this;
    }
    public static SimplePacketListenerPlugin getInstance() {
        if(pluginInstance == null) {
            pluginInstance = new SimplePacketListenerPlugin(Bukkit.getServer(), JavaPlugin.getPlugin(QuickMorph.class).getLogger(), JavaPlugin.getPlugin(QuickMorph.class));
        }
        return pluginInstance;
    }

    public void onLoad()
    {
        try
        {
            logger.info(NmsReflection.class.getName());
            // Loading reflection toolkit.
            Class.forName(NmsReflection.class.getName());
        } catch (Throwable fatal)
        {
            logger.severe("Couldn't initialize reflective class ! Did the package schema has changed ?");
            fatal.printStackTrace();
        }
    }

    public void onEnable()
    {
        server.getPluginManager().registerEvents(new ListenForInjection(), basePlugin);

        // Re-inject custom handler to all active channels.
        try
        {
            // Removing injected handler to any server's channels.
            List<Channel> channels = InjectionUtils.getServerChannels(server);
            channels.forEach((ch) -> {
                Player player = getPlayerFromAddress((InetSocketAddress) ch.remoteAddress());
                InjectionUtils.injectCustomHandler(player, ch);
            });
        } catch (ReflectiveOperationException ex)
        {
            logger.warning("Couldn't get active server's channels !");
            ex.printStackTrace();
        }

    }

    public void onDisable()
    {
        try
        {
            // Removing injected handler to any server's channels.
            List<Channel> channels = InjectionUtils.getServerChannels(server);
            channels.forEach(InjectionUtils::removeCustomHandler);
        } catch (ReflectiveOperationException ex)
        {
            logger.warning("Couldn't get active server's channels !");
            ex.printStackTrace();
        }
    }

    /**
     * Retrieve a player from a socket address.
     * 
     * @param addr - player's address.
     * @return player connected from {@code addr} or {@code null} if not found.
     */
    private Player getPlayerFromAddress(InetSocketAddress addr)
    {
        Player find = null;
        Iterator<? extends Player> onlinePlayers = server.getOnlinePlayers().iterator();

        while (onlinePlayers.hasNext() && find == null)
        {
            Player player = onlinePlayers.next();
            if (Objects.equals(player.getAddress(), addr))
                find = player;
        }

        return find;
    }

    /**
     * Listener class used to listen to all needed activity (like player join, quit,
     * server ping, etc.).
     * 
     * @author DrogoniEntity
     */
    private class ListenForInjection implements Listener
    {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerJoin(PlayerJoinEvent event)
        {
            Player player = event.getPlayer();

            try
            {
                Channel channel = InjectionUtils.getPlayerChannel(player);
                InjectionUtils.injectCustomHandler(player, channel);
            } catch (ReflectiveOperationException ex)
            {
                logger.severe("Couldn't inject packet handler to " + player.getName());
                ex.printStackTrace();
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onPlayerQuit(PlayerQuitEvent event)
        {
            Player player = event.getPlayer();

            try
            {
                Channel channel = InjectionUtils.getPlayerChannel(player);
                InjectionUtils.removeCustomHandler(channel);
            } catch (ReflectiveOperationException ex)
            {
                logger.severe("Couldn't remove packet handler from " + player.getName());
                ex.printStackTrace();
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onServerListPing(ServerListPingEvent event)
        {
            try
            {
                List<Channel> channels = InjectionUtils.getServerChannels(server);

                for (Channel channel : channels) {
                    InjectionUtils.injectCustomHandler(null, channel);
                }
            } catch (ReflectiveOperationException ex)
            {
                logger.warning("Couldn't get active server's channels !");
                ex.printStackTrace();
            }
        }
    }
}
