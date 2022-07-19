package fr.theskyblockman.quickmorph;

import org.bukkit.event.Listener;

public interface ActionableListener extends Listener {
    void setActivated(boolean value);
    boolean isActivated();
}
