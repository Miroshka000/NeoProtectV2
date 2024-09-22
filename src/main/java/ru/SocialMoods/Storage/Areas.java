package ru.SocialMoods.Storage;

import cn.nukkit.level.Location;

public class Areas {
    private Location location;
    private int blockId;

    public Areas(Location location, int blockId) {
        this.location = location;
        this.blockId = blockId;
    }

    public Location getLocation() {
        return location;
    }

    public int getBlockId() {
        return blockId;
    }
}