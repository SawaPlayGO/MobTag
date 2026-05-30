package me.abood8001.mobtag.hooks;

import me.abood8001.mobtag.MobTag;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.attribute.Attribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Registers %mobtag_<placeholder>% via PlaceholderAPI.
 *
 * Available placeholders (for use in other plugins via PAPI):
 *   %mobtag_nearby_mob_health%   - health of the nearest tagged mob to the player
 *   %mobtag_nearby_mob_maxhealth% - max health of the nearest tagged mob
 *   %mobtag_nearby_mob_name%     - name of the nearest tagged mob
 */
public class PAPIHook extends PlaceholderExpansion {

    private final MobTag plugin;

    public PAPIHook(MobTag plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "mobtag";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Abood_8001";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // Don't unregister on reload
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        double range = plugin.getMobTagConfig().getDisplayRange();
        LivingEntity nearest = getNearestTaggedMob(player, range);

        switch (params.toLowerCase()) {
            case "nearby_mob_health":
                if (nearest == null) return "0";
                return String.valueOf(Math.round(nearest.getHealth() * 10.0) / 10.0);

            case "nearby_mob_maxhealth":
                if (nearest == null) return "0";
                double max = nearest.getMaxHealth();
                return String.valueOf(Math.round(max * 10.0) / 10.0);

            case "nearby_mob_name":
                if (nearest == null) return "None";
                return nearest.getCustomName() != null ? nearest.getCustomName() : nearest.getType().name();

            default:
                return null;
        }
    }

    private LivingEntity getNearestTaggedMob(Player player, double range) {
        LivingEntity nearest = null;
        double closestDist = range * range;

        for (var uid : plugin.getTagManager().getTags().keySet()) {
            var entity = player.getServer().getEntity(uid);
            if (!(entity instanceof LivingEntity le)) continue;
            if (le.getWorld() != player.getWorld()) continue;
            double dist = le.getLocation().distanceSquared(player.getLocation());
            if (dist < closestDist) {
                closestDist = dist;
                nearest = le;
            }
        }
        return nearest;
    }
}
