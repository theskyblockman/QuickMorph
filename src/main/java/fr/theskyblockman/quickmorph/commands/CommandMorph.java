package fr.theskyblockman.quickmorph.commands;

import fr.theskyblockman.quickmorph.QuickMorph;
import fr.theskyblockman.quickmorph.permission.MorphGroup;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CommandMorph implements TabExecutor {
    public static boolean entityValid(String entityName, MorphGroup group) {

        return false;
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + QuickMorph.currentLanguage.getString("error-not-player"));
            return false;
        }
        MorphGroup playersGroup = MorphGroup.fromPriority(QuickMorph.associatedGroups.get(player.getUniqueId()));

        assert playersGroup != null;
        if(!playersGroup.canCreate && playersGroup.morphOthers) {
            sender.sendMessage(ChatColor.RED + QuickMorph.currentLanguage.getString("error-permission"));
            return false;
        }




        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) {
            return new ArrayList<>();
        }


        List<String> argumentsToReturn = new ArrayList<>();

        if(args.length == 1) {
            for (EntityType entityType : EntityType.values()) {
                if(entityType.isAlive()) {
                    argumentsToReturn.add(entityType.name().toLowerCase());
                }
            }
        }
        return argumentsToReturn;
    }
}
