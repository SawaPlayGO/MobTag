package me.abood8001.mobtag.hooks;

import io.lumine.mythic.bukkit.MythicBukkit;
import me.abood8001.mobtag.MobTag;
import org.bukkit.entity.LivingEntity;

public class MythicMobsHook {

    private final MobTag plugin;

    public MythicMobsHook(MobTag plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns true if the given entity is a MythicMob.
     */
    public boolean isMythicMob(LivingEntity entity) {
        try {
            return MythicBukkit.inst().getMobManager().isActiveMob(entity.getUniqueId());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the internal MythicMob name for the entity,
     * or the entity type name as a fallback.
     */
    public String getMythicMobName(LivingEntity entity) {
        try {
            var activeMob = MythicBukkit.inst().getMobManager().getActiveMob(entity.getUniqueId());
            if (activeMob.isPresent()) {
                return activeMob.get().getType().getInternalName();
            }
        } catch (Exception ignored) {}
        return entity.getType().name();
    }
}
