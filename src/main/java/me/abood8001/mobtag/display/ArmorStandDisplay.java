package me.abood8001.mobtag.display;

import io.lumine.mythic.bukkit.utils.lib.jooq.True;
import me.abood8001.mobtag.MobTag;
import me.abood8001.mobtag.MobTagConfig;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class ArmorStandDisplay implements TagDisplay {

    private ArmorStand stand;
    private final MobTag plugin;

    public ArmorStandDisplay(Location loc, String text, MobTag plugin) {
        this.plugin = plugin;
        this.stand = loc.getWorld().spawn(loc, ArmorStand.class, as -> {
            as.setVisible(false);
            as.setGravity(false);
            as.setCanPickupItems(false);
            as.setCustomNameVisible(this.visibleByDefault());
            as.setCustomName(text);
            as.setInvulnerable(true);
            as.setSilent(true);
            as.setSmall(true);
            as.setMarker(true);
            as.setPersistent(false);
        });
    }

    @Override
    public void update(Location location, String text, Player viewer) {
        if (stand == null || stand.isDead()) return;
        stand.teleport(location);
        stand.setCustomName(text);
    }

    @Override
    public void remove() {
        if (stand != null && !stand.isDead()) stand.remove();
    }

    @Override
    public boolean isDead() {
        return stand == null || stand.isDead();
    }

    public void showFor() {
        if (stand == null || stand.isDead()) return;
        stand.setCustomNameVisible(true);
    }

    public void hideFor() {
        if (stand == null || stand.isDead()) return;
        stand.setCustomNameVisible(false);
    }

    private boolean visibleByDefault() {
        MobTagConfig cfg = plugin.getMobTagConfig();
        return !cfg.getVisibilityMode().equals("LOOKING_AT");
    }

}