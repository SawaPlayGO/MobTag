package me.abood8001.mobtag;


import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkUnloadEvent;


public class MobTagListener implements Listener {

    private final MobTag plugin;

    public MobTagListener(MobTag plugin) {
        this.plugin = plugin;
    }

    /**
     * Track who last damaged the mob (used for DAMAGER visibility mode).
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity instanceof ArmorStand) return;

        if (event.getDamager() instanceof Player player) {
            plugin.getTagManager().setLastDamager(entity.getUniqueId(), player.getUniqueId());
        }
    }

    /**
     * Remove the tag when the mob dies.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        plugin.getTagManager().removeTag(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (var display : plugin.getTagManager().getTags().values()) {
            if (display instanceof me.abood8001.mobtag.display.PacketDisplay pd) {
                pd.removeFor(player);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (var display : plugin.getTagManager().getTags().values()) {
                if (display instanceof me.abood8001.mobtag.display.PacketDisplay pd) {
                    pd.removeFor(player);
                }
            }
        }, 20L); // 1-second delay
    }



    /**
     * Clean up tags for mobs in unloaded chunks.
     */
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (var entity : event.getChunk().getEntities()) {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                plugin.getTagManager().removeTag(entity.getUniqueId());
            }
        }
    }
}
