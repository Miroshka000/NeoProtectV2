package ru.SocialMoods;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityExplodeEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.level.Location;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import ru.SocialMoods.Storage.Areas;

public class EventListener implements Listener {
    private NeoProtect plugin;
    private Map<Integer, Integer> protectionBlocks;
    private int maximumProtections;

    public EventListener(NeoProtect plugin) {
        this.plugin = plugin;
        this.protectionBlocks = new HashMap<>();
        Map<String, Object> blocks = plugin.config.getSection("protection-blocks").getAllMap();
        for (Map.Entry<String, Object> entry : blocks.entrySet()) {
            this.protectionBlocks.put(Integer.parseInt(entry.getKey()), (Integer) entry.getValue());
        }
        this.maximumProtections = plugin.config.getInt("maximum-protections");
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onExplode(EntityExplodeEvent event) {
        List<Block> list = event.getBlockList();
        String owner = null;
        Location explosionLocation = null;

        for (Block block : list) {
            if (this.blockInProtection(block.getLocation())) {
                owner = this.getOwnerByLocation(block.getLocation());
                explosionLocation = block.getLocation();
                break;
            }
        }

        if (owner != null && explosionLocation != null) {
            Long chatId = this.plugin.getChatIdByPlayerName(owner);
            if (chatId != null) {
                String messageTemplate = this.plugin.config.getString("messages.explosion-detected", "Взрыв произошел в вашем регионе. Координаты: {x}, {y}, {z}");
                String message = messageTemplate
                        .replace("{x}", String.valueOf(explosionLocation.getX()))
                        .replace("{y}", String.valueOf(explosionLocation.getY()))
                        .replace("{z}", String.valueOf(explosionLocation.getZ()));
                this.plugin.getTgBot().sendMessage(chatId, message);
                playerRemoveProtection(explosionLocation, plugin.getServer().getPlayer(owner));
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (this.playerInRangeProtection(block.getLocation(), player)) {
            player.sendMessage(this.getMessage("block-use-denied").replace("%block_name%", block.getName()));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (this.protectionBlocks.containsKey(block.getId())) {
            if (this.playerRemoveProtection(block.getLocation(), player)) {
                player.sendMessage(this.getMessage("block-break-own"));
            } else if (this.playerInRangeProtection(block.getLocation(), player)) {
                event.setCancelled(true);
                player.sendMessage(this.getMessage("block-break-denied"));
            }
        } else if (this.playerInRangeProtection(block.getLocation(), player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (this.protectionBlocks.containsKey(block.getId())) {
            int numPlayerProtections = this.getNumPlayerProtections(player);
            if (numPlayerProtections >= this.maximumProtections) {
                player.sendMessage(this.getMessage("max-protections-reached"));
                event.setCancelled(true);
            } else if (this.playerInRangeProtection(block.getLocation(), player)) {
                event.setCancelled(true);
            } else {
                int radius = this.protectionBlocks.get(block.getId());
                List<Location> protectionArea = this.ProtectionArea(block.getLocation(), radius);
                boolean isProtected = false;
                for (Location loc : protectionArea) {
                    if (this.blockInProtection(loc)) {
                        isProtected = true;
                        break;
                    }
                }

                if (isProtected) {
                    player.sendMessage(this.getMessage("protection-overlap"));
                    event.setCancelled(true);
                } else {
                    this.playerPlaceProtection(block.getLocation(), player, block.getId());
                    int remaining = this.maximumProtections - numPlayerProtections - 1;
                    player.sendMessage(this.getMessage("protection-placed"));
                    player.sendMessage(this.getMessage("protections-remaining").replace("%remaining%", String.valueOf(remaining)));
                    player.sendMessage(this.getMessage("protection-radius-created").replace("%radius%", String.valueOf(radius)));
                }
            }
        } else if (this.playerInRangeProtection(block.getLocation(), player)) {
            event.setCancelled(true);
        }
    }

    private String getOwnerByLocation(Location loc) {
        Set<Entry<String, List<Areas>>> set = this.plugin.map.entrySet();
        for (Entry<String, List<Areas>> entry : set) {
            List<Areas> areas = entry.getValue();
            for (Areas area : areas) {
                if (loc.distance(area.getLocation()) < this.protectionBlocks.getOrDefault(area.getBlockId(), 0)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private int getNumPlayerProtections(Player player) {
        List<Areas> list = this.plugin.map.get(player.getName());
        return list != null ? list.size() : 0;
    }

    private void playerPlaceProtection(Location loc, Player player, int blockId) {
        List<Areas> list = this.plugin.map.getOrDefault(player.getName(), new ArrayList<>());
        list.add(new Areas(loc, blockId));
        this.plugin.map.put(player.getName(), list);
    }

    private boolean playerRemoveProtection(Location loc, Player player) {
        List<Areas> list = this.plugin.map.get(player.getName());
        if (list != null) {
            Iterator<Areas> iterator = list.iterator();
            while (iterator.hasNext()) {
                Areas area = iterator.next();
                if (area.getLocation().equals(loc)) {
                    iterator.remove();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean playerInRangeProtection(Location loc, Player player) {
        Set<Entry<String, List<Areas>>> set = this.plugin.map.entrySet();
        for (Entry<String, List<Areas>> entry : set) {
            List<Areas> areas = entry.getValue();
            for (Areas area : areas) {
                if (loc.distance(area.getLocation()) < this.protectionBlocks.getOrDefault(area.getBlockId(), 0)) {
                    if (!entry.getKey().equals(player.getName())) {
                        player.sendPopup(this.getMessage("area-owned-popup").replace("%owner%", entry.getKey()));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean blockInProtection(Location loc) {
        Set<Entry<String, List<Areas>>> set = this.plugin.map.entrySet();
        for (Entry<String, List<Areas>> entry : set) {
            List<Areas> areas = entry.getValue();
            for (Areas area : areas) {
                if (loc.distance(area.getLocation()) < this.protectionBlocks.getOrDefault(area.getBlockId(), 0)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Location> ProtectionArea(Location loc, int radius) {
        List<Location> list = new ArrayList<>();
        double y = loc.y;

        for (double i = 0.0D; i < 360.0D; i += 0.1D) {
            double angle = i * Math.PI / 180.0D;
            double x = loc.getX() + radius * Math.cos(angle);
            double z = loc.getZ() + radius * Math.sin(angle);
            list.add(new Location(x, y, z));
        }

        return list;
    }

    private String getMessage(String key) {
        return this.plugin.config.getString("messages." + key, "Сообщение не найдено: " + key);
    }
}