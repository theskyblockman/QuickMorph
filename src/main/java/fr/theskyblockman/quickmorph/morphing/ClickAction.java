package fr.theskyblockman.quickmorph.morphing;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public enum ClickAction {
    UN_MORPH(ChatColor.GOLD + "un-morph", Morph::askStop),
    ENABLE_FREE_VIEW(ChatColor.AQUA + "Enable free looking", morph -> morph.freeView = true),
    DISABLE_FREE_VIEW(ChatColor.AQUA + "Disable free looking", morph -> morph.freeView = false),
    ENABLE_FORCE_AI(ChatColor.GOLD + "Enable entity AI", morph -> morph.setEntityAI(true)),
    DISABLE_FORCE_AI(ChatColor.GOLD + "Disable entity AI", morph -> morph.setEntityAI(false)),
    ENABLE_FREE_MOVE(ChatColor.GRAY + "Enable free moving", morph -> morph.freeMove = true),
    DISABLE_FREE_MOVE(ChatColor.GRAY + "Disable free moving", morph -> morph.freeMove = false),
    OPEN_MANUAL(ChatColor.BLACK + "Open manual", (morph) -> {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        assert meta != null;
        meta.setTitle("Manual");
        meta.setPages("""
                Morphing parameter:
                entity-AI: Make entity move themselves or
                not (you can do doing right-click at a
                block you make your entity move to this
                block)
                """,
                """
                Morphing parameter:
                free-looking: Synchronise or not your head
                position with the entity
                """,
                """
                Morphing parameter:
                free-moving: Synchronise or not your body
                position with the entity
                """);
        meta.setAuthor("theskyblockman");
        book.setItemMeta(meta);
        morph.getPlayer().openBook(book);
    }), REAPPEAR(ChatColor.RED + "Reappear as a player", morph -> morph.stop(true)),
    ENABLE_SOUL_MODE(ChatColor.YELLOW + "Reappear as a soul (invisible)", Morph::startSoulMode),
    CLOSE_GUI(ChatColor.GREEN + "Cancel", morph -> morph.getPlayer().closeInventory()),
    STOP_MORPH(ChatColor.DARK_RED + "Stop morph", morph -> morph.stop(true));
    public final String displayName;
    public final ClickRunnable clickAction;
    ClickAction(String displayName, ClickRunnable clickAction) {
        this.displayName = displayName;
        this.clickAction = clickAction;
    }

    public String toID() {
        return this.displayName + "." + this.name();
    }

    public static ClickAction fromID(String id) {
        for (ClickAction action : values()) {
            if(action.toID().equals(id)) {
                return action;
            }
        }
        return UN_MORPH;
    }
}
