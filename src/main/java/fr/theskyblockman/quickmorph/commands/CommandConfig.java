package fr.theskyblockman.quickmorph.commands;

import com.google.common.collect.Lists;
import fr.theskyblockman.quickmorph.QuickMorph;
import fr.theskyblockman.quickmorph.permission.GroupType;
import fr.theskyblockman.quickmorph.permission.MorphGroup;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandConfig implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length <= 1) {
            sender.sendMessage(ChatColor.RED + QuickMorph.currentLanguage.getString("not-enough-arguments"));
        }
        /*
        Schemas:
        - /config edit-group default vulnerable false
        - /config add-group fr.theskyblockman.config.use
        - /config remove-group fr.theskyblockman.config.use
         */

        switch(args[0]) {
            case "edit-group" -> {
                if(args.length < 4) {
                    sender.sendMessage(ChatColor.RED + QuickMorph.currentLanguage.getString("not-enough-arguments"));
                    return false;
                }
                if(MorphGroup.groupArgs.get(args[2]) == null) {
                    sender.sendMessage(ChatColor.RED + QuickMorph.currentLanguage.getString("parameter-not-existing").replace("{0}", args[2]));
                    return false;
                }
                if(MorphGroup.groupArgs.get(args[2]).get(args[3]) == null && !MorphGroup.groupArgs.get(args[2]).isEmpty()) {
                    sender.sendMessage(ChatColor.RED + QuickMorph.currentLanguage.getString("parameter-not-existing").replace("{0}", args[3]));
                    return false;
                }
                String parameterPath = "";
                MorphGroup group = null;
                for (MorphGroup morphGroup : QuickMorph.morphGroups) {
                    if(Objects.equals(morphGroup.groupName, args[1])) {
                        group = morphGroup;
                        if(morphGroup.groupType == GroupType.CUSTOM) {
                            parameterPath = "custom.";
                        }
                        break;
                    }
                }
                if(group == null) {
                    sender.sendMessage(ChatColor.RED + QuickMorph.currentLanguage.getString("parameter-not-existing").replace("{0}", args[1]));
                    return false;
                }
                parameterPath += group.groupName + "." + args[2];
                if(group.groupType == GroupType.CUSTOM) {
                    parameterPath = parameterPath.replace(group.groupName, group.groupName.replace('.', '-'));
                }
                if(MorphGroup.groupArgs.get(args[2]).isEmpty()) {
                    QuickMorph.configuration.set(parameterPath, Integer.parseInt(args[3]));
                } else {
                    QuickMorph.configuration.set(parameterPath, MorphGroup.groupArgs.get(args[2]).get(args[3]));
                }


                sender.sendMessage(ChatColor.GREEN + QuickMorph.currentLanguage.getString("command-config-success"));
                QuickMorph.isConfigEdited = true;
                return true;
            }
            case "add-group" -> {
                if(args.length < 2) {
                    sender.sendMessage(ChatColor.RED + QuickMorph.currentLanguage.getString("not-enough-arguments"));
                }
                for (MorphGroup morphGroup : QuickMorph.morphGroups) {
                    if(Objects.equals(morphGroup.groupName, args[1])) {
                        sender.sendMessage(ChatColor.RED + QuickMorph.currentLanguage.getString("command-config-group-already-exists"));
                        return false;
                    }
                }
                ConfigurationSection newGroup = QuickMorph.configuration.createSection("custom." + args[1]);
                for(String arg : MorphGroup.groupArgs.keySet()) {
                    Object element;
                    if (Lists.newArrayList(MorphGroup.groupArgs.get(arg).keySet()).isEmpty()) {
                        element = 0;
                    } else {
                        element = Lists.newArrayList(MorphGroup.groupArgs.get(arg).keySet()).get(0);
                    }
                    newGroup.set(arg, element);
                }

                QuickMorph.morphGroups.add(new MorphGroup(newGroup, GroupType.CUSTOM));
                sender.sendMessage(ChatColor.GREEN + QuickMorph.currentLanguage.getString("command-config-success"));
                QuickMorph.isConfigEdited = true;
            }
            case "remove-group" -> {
                if(args.length < 2) {
                    sender.sendMessage(ChatColor.RED + QuickMorph.currentLanguage.getString("not-enough-arguments"));
                }
                MorphGroup group = null;

                for (MorphGroup morphGroup : QuickMorph.morphGroups) {
                    if(Objects.equals(morphGroup.groupName, args[1])) {
                        group = morphGroup;
                        break;
                    }
                }

                if(group == null) {
                    sender.sendMessage(ChatColor.RED + QuickMorph.currentLanguage.getString("command-config-group-dont-exists"));
                    return false;
                }
                QuickMorph.morphGroups.remove(group);
                sender.sendMessage(ChatColor.GREEN + QuickMorph.currentLanguage.getString("command-config-success"));
                QuickMorph.isConfigEdited = true;
            }

            case "show-group" -> {
                if(args.length < 2) {
                    sender.sendMessage(ChatColor.RED + QuickMorph.currentLanguage.getString("not-enough-arguments"));
                }
                MorphGroup group = null;

                for (MorphGroup morphGroup : QuickMorph.morphGroups) {
                    if(Objects.equals(morphGroup.groupName, args[1])) {
                        group = morphGroup;
                        break;
                    }
                }

                if(group == null) {
                    sender.sendMessage(ChatColor.RED + QuickMorph.currentLanguage.getString("command-config-group-dont-exists"));
                    return false;
                }

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Here are all the parameters for the group \"{0}\": \n".replace("{0}", group.groupName));
                for (String arg : MorphGroup.groupArgs.keySet()) {
                    stringBuilder.append(arg).append(" -> ").append(Objects.requireNonNull(group.rawSection.get(arg))).append("\n");
                }
                stringBuilder.append("This group is a \"").append(group.groupType.displayName).append("\" group.");
                sender.sendMessage(stringBuilder.toString());
            }
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> argsToReturn = new ArrayList<>();

        if(args.length == 1) {
            argsToReturn.add("edit-group");
            argsToReturn.add("add-group");
            argsToReturn.add("remove-group");
            argsToReturn.add("show-group");
        } else if(args.length == 2) {
            switch (args[0]) {
                case "edit-group", "remove-group", "show-group" -> {
                    for(MorphGroup morphGroup : QuickMorph.morphGroups) {
                        argsToReturn.add(morphGroup.groupName);
                    }
                }
                default -> {
                    return argsToReturn;
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("edit-group")) {
            argsToReturn = Lists.newArrayList(MorphGroup.groupArgs.keySet());
        } else if(args.length == 4 && args[0].equalsIgnoreCase("edit-group")) {
            if(MorphGroup.groupArgs.get(args[2]) == null) return argsToReturn;

            argsToReturn = Lists.newArrayList(MorphGroup.groupArgs.get(args[2]).keySet());
        }

        return argsToReturn;
    }
}