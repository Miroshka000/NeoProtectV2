package ru.SocialMoods.Storage;

import cn.nukkit.Server;
import cn.nukkit.level.Location;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Areas implements Serializable {
    private int blockId;
    private Set<String> allowedPlayers;

    private double x;
    private double y;
    private double z;
    private String levelName;

    public Areas(Location location, int blockId) {
        this.blockId = blockId;
        this.allowedPlayers = new HashSet<>();
        this.x = location.x;
        this.y = location.y;
        this.z = location.z;
        this.levelName = location.getLevel().getName();
    }

    public Location getLocation() {
        return new Location(x, y, z, Server.getInstance().getLevelByName(levelName));
    }

    public int getBlockId() {
        return blockId;
    }

    public Set<String> getAllowedPlayers() {
        return allowedPlayers;
    }

    public void addPlayer(String playerName) {
        allowedPlayers.add(playerName);
    }

    public void removePlayer(String playerName) {
        allowedPlayers.remove(playerName);
    }

    public boolean isAllowed(String playerName) {
        return allowedPlayers.contains(playerName);
    }
}