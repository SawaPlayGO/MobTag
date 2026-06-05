package me.abood8001.mobtag.display;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface TagDisplay {
    void update(Location location, String text, Player viewer);
    void remove();
    boolean isDead();
}