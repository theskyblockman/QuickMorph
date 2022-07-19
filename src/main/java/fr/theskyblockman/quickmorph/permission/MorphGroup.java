package fr.theskyblockman.quickmorph.permission;

import fr.theskyblockman.quickmorph.QuickMorph;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class MorphGroup {
    public static MorphGroup fromPriority(int priority) {
        for (MorphGroup morphGroup : QuickMorph.morphGroups) {
            if(morphGroup.priority == priority) {
                return morphGroup;
            }
        }
        return null;
    }


    /*
        args:
        -  vulnerable: boolean
        -  player-vulnerable: boolean
        -  health-linked: boolean
        -  health-owner: string (player, entity)
        -  can-use-all: boolean
        -  can-create: boolean
        -  entity-loot: boolean
        -  re-control: int
        -  morph-others: boolean
        -  max-entity-distance: int
     */
    public boolean vulnerable;
    public boolean playerVulnerable;
    public boolean healthLinked;
    public String healthOwner;
    public boolean canUseAll;
    public boolean canCreate;
    public boolean entityLoot;
    public int reControl;
    public String groupName;
    public GroupType groupType;
    public ConfigurationSection rawSection;
    public int priority;
    public boolean morphOthers;
    public int maxEntityDistance;

    public static Map<String, Map<String, Object>> groupArgs = new HashMap<>();
    public static Map<String, Object> argsBoolean = new HashMap<>();
    public static Map<String, Object> argsHealthOwner = new HashMap<>();

    static {

        argsBoolean.put("true", true);
        argsBoolean.put("false", false);
        argsHealthOwner.put("entity", "entity");
        argsHealthOwner.put("player", "player");


        groupArgs.put("vulnerable", argsBoolean);
        groupArgs.put("player-vulnerable", argsBoolean);
        groupArgs.put("health-linked", argsBoolean);
        groupArgs.put("health-owner", argsHealthOwner);
        groupArgs.put("can-use-all", argsBoolean);
        groupArgs.put("can-create", argsBoolean);
        groupArgs.put("entity-loot", argsBoolean);
        groupArgs.put("re-control", new HashMap<>());
        groupArgs.put("priority", new HashMap<>());
        groupArgs.put("morph-others", argsBoolean);
        groupArgs.put("max-entity-distance", new HashMap<>());
    }


    public MorphGroup(ConfigurationSection configurationSection, GroupType groupType) {
        groupName = configurationSection.getName().replace('-', '.');
        rawSection = configurationSection;
        this.groupType = groupType;
        updateGroup();
    }

    public void updateGroup() {
        vulnerable = rawSection.getBoolean("vulnerable");
        playerVulnerable = rawSection.getBoolean("player-vulnerable");
        healthLinked = rawSection.getBoolean("health-linked");
        healthOwner = rawSection.getString("health-owner");
        canUseAll = rawSection.getBoolean("can-use-all");
        canCreate = rawSection.getBoolean("can-create");
        entityLoot = rawSection.getBoolean("entity-loot");
        reControl = rawSection.getInt("re-control");
        priority = rawSection.getInt("priority");
        morphOthers = rawSection.getBoolean("morph-others");
        maxEntityDistance = rawSection.getInt("max-entity-distance");
    }
}
