package fr.theskyblockman.quickmorph.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class EntityIterator implements Iterator<Entity> {
    @Override
    public boolean hasNext() {
        return remainingEntities.size() != 0;
    }

    @Override
    public Entity next() {
        return remainingEntities.remove(remainingEntities.size() - 1);
    }

    public final int maxDistance;
    public final int maxEntities;
    public final Location statingLocation;
    public final boolean passThroughBlocks;
    public List<Entity> foundEntities = new ArrayList<>();
    private final List<Entity> remainingEntities;

    public EntityIterator(int maxDistance, Location location, boolean throughBlocks, int maxEntities) {
        this.maxDistance = maxDistance;
        this.statingLocation = location;
        this.passThroughBlocks = throughBlocks;
        this.maxEntities = maxEntities;

        Location currentLocation = statingLocation;
        BoundingBox rayStop = null;

        if(!throughBlocks) {
            BlockIterator iterator = new BlockIterator(Objects.requireNonNull(location.getWorld()), location.toVector(), location.getDirection(), 0, maxDistance);

            while(iterator.hasNext()) {
                Block foundBlock = iterator.next();
                if(foundBlock.getType() != Material.AIR) {
                    rayStop = foundBlock.getBoundingBox();
                }
            }
        }

        Vector initialLooking = location.getDirection();
        for (int i = 0; i < maxDistance * 5; i++) {
            currentLocation.add(initialLooking.divide(new Vector(5, 5, 5)));

            for(org.bukkit.entity.Entity entity : Objects.requireNonNull(location.getWorld()).getEntities()) {
                if(rayStop != null && rayStop.contains(entity.getLocation().toVector())) break;
                if(entity.getBoundingBox().contains(currentLocation.toVector())) {
                    foundEntities.add(entity);
                    if(this.maxEntities == this.foundEntities.size()) {
                        break;
                    }
                }
            }
        }

        remainingEntities = foundEntities;
    }
}
