package me.abood8001.mobtag;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MobTagManager {

    private final MobTag plugin;

    // Maps mob UUID -> ArmorStand tag entity
    private final Map<UUID, ArmorStand> tags = new ConcurrentHashMap<>();

    // Maps mob UUID -> UUID of last damager
    private final Map<UUID, UUID> lastDamager = new ConcurrentHashMap<>();

    private BukkitTask task;

    // Passive mob types
    private static final Set<EntityType> PASSIVE_TYPES = EnumSet.of(
            EntityType.COW, EntityType.PIG, EntityType.SHEEP, EntityType.CHICKEN,
            EntityType.RABBIT, EntityType.HORSE, EntityType.DONKEY, EntityType.MULE,
            EntityType.LLAMA, EntityType.CAT, EntityType.WOLF, EntityType.PARROT,
            EntityType.TURTLE, EntityType.FOX, EntityType.BEE, EntityType.AXOLOTL,
            EntityType.FROG, EntityType.TADPOLE, EntityType.SNIFFER, EntityType.ALLAY,
            EntityType.STRIDER, EntityType.MOOSHROOM, EntityType.GOAT
    );

    // Boss types
    private static final Set<EntityType> BOSS_TYPES = EnumSet.of(
            EntityType.ENDER_DRAGON, EntityType.WITHER
    );

    public MobTagManager(MobTag plugin) {
        this.plugin = plugin;
    }

    public void startTask() {
        MobTagConfig cfg = plugin.getMobTagConfig();
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::updateTags,
                cfg.getUpdateInterval(), cfg.getUpdateInterval());
    }

    public void reload() {
        if (task != null) task.cancel();
        startTask();
    }

    private void updateTags() {
        MobTagConfig cfg = plugin.getMobTagConfig();
        double range = cfg.getDisplayRange();
        double rangeSquared = range * range;

        // Collect all living mobs across all worlds
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                if (!shouldTag(entity)) continue;

                // Find if any eligible player is nearby
                Player viewer = getViewer(entity, rangeSquared, cfg.getVisibilityMode());

                if (viewer != null) {
                    showTag(entity, viewer);
                } else {
                    removeTag(entity.getUniqueId());
                }
            }
        }

        // Clean up tags for mobs that are gone
        tags.entrySet().removeIf(entry -> {
            UUID uid = entry.getKey();
            ArmorStand stand = entry.getValue();
            if (Bukkit.getEntity(uid) == null) {
                if (!stand.isDead()) stand.remove();
                lastDamager.remove(uid);
                return true;
            }
            return false;
        });
    }

    private Player getViewer(LivingEntity entity, double rangeSquared, String mode) {
        if ("DAMAGER".equals(mode)) {
            UUID damagerUUID = lastDamager.get(entity.getUniqueId());
            if (damagerUUID == null) return null;
            Player damager = Bukkit.getPlayer(damagerUUID);
            if (damager == null || !damager.isOnline()) return null;
            if (damager.getWorld() != entity.getWorld()) return null;
            double dist = damager.getLocation().distanceSquared(entity.getLocation());
            return dist <= rangeSquared ? damager : null;
        } else {
            // ALL mode: find nearest player in range
            Player nearest = null;
            double closestDist = rangeSquared;
            for (Player p : entity.getWorld().getPlayers()) {
                if (p.getGameMode() == org.bukkit.GameMode.SPECTATOR) continue;
                double dist = p.getLocation().distanceSquared(entity.getLocation());
                if (dist <= closestDist) {
                    closestDist = dist;
                    nearest = p;
                }
            }
            return nearest;
        }
    }

    private boolean shouldTag(LivingEntity entity) {
        MobTagConfig cfg = plugin.getMobTagConfig();

        // Skip players
        if (entity instanceof Player && cfg.isIgnorePlayers()) return false;

        // Skip ArmorStands (our own tags)
        if (entity instanceof ArmorStand) return false;

        if (plugin.getMobTagConfig().getBlacklistedWorlds().contains(entity.getWorld().getName())) return false;


        if (entity.hasMetadata("NPC")) return false;
        if (entity.hasMetadata("shopkeeper")) return false;
        if (entity.getClass().getName().toLowerCase().contains("simplepets")) return false;


        // Skip bosses if disabled
        if (BOSS_TYPES.contains(entity.getType()) && !cfg.isShowOnBosses()) return false;

        // Skip passive mobs if configured
        if (cfg.isIgnorePassive() && PASSIVE_TYPES.contains(entity.getType())) return false;

        return true;
    }

    public void showTag(LivingEntity entity, Player viewer) {
        MobTagConfig cfg = plugin.getMobTagConfig();
        String text = buildTagText(entity, viewer, cfg);

        UUID uid = entity.getUniqueId();
        Location tagLoc = getTagLocation(entity, cfg);

        ArmorStand stand = tags.get(uid);

        if (stand == null || stand.isDead()) {
            stand = spawnTagStand(tagLoc, text);
            tags.put(uid, stand);
        } else {
            stand.teleport(tagLoc);
            stand.setCustomName(text);
        }
    }

    private Location getTagLocation(LivingEntity entity, MobTagConfig cfg) {
        double height = entity.getHeight() + cfg.getTagHeightOffset();
        return entity.getLocation().clone().add(0, height, 0);
    }

    private ArmorStand spawnTagStand(Location loc, String text) {
        ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class, as -> {
            as.setVisible(false);
            as.setGravity(false);
            as.setCanPickupItems(false);
            as.setCustomNameVisible(true);
            as.setCustomName(text);
            as.setInvulnerable(true);
            as.setSilent(true);
            as.setSmall(true);
            as.setMarker(true);
            as.setPersistent(false);
        });
        return stand;
    }

    private String buildTagText(LivingEntity entity, Player viewer, MobTagConfig cfg) {
        double current = entity.getHealth();
        double max = entity.getMaxHealth();
        current = Math.round(current * 10.0) / 10.0;
        max = Math.round(max * 10.0) / 10.0;
        double percent = (entity.getHealth() / max) * 100.0;

        String mobName = getMobName(entity, cfg);
        String bar = buildBar(entity.getHealth(), max, cfg);

        String format = cfg.getTagFormat();

        // MythicMobs custom format
        if (plugin.isMythicMobsEnabled() && plugin.getMythicMobsHook().isMythicMob(entity)) {
            String mythicFmt = cfg.getMythicCustomFormat();
            if (mythicFmt != null && !mythicFmt.isEmpty()) {
                format = mythicFmt;
                String mythicName = plugin.getMythicMobsHook().getMythicMobName(entity);
                format = format.replace("{mythic_name}", mythicName);
            }
        }

        format = format
                .replace("{name}", mobName)
                .replace("{type}", entity.getType().name())
                .replace("{current}", String.valueOf(current))
                .replace("{max}", String.valueOf(max))
                .replace("{bar}", bar)
                .replace("{percent}", String.format("%.1f", percent));

        // PlaceholderAPI support
        if (plugin.isPapiEnabled()) {
            try {
                format = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(viewer, format);
            } catch (Exception ignored) {}
        }

        return colorize(format);
    }

    private String getMobName(LivingEntity entity, MobTagConfig cfg) {
        if (entity.getCustomName() != null && !entity.getCustomName().isEmpty()) {
            return entity.getCustomName();
        }
        if (plugin.isMythicMobsEnabled() && plugin.getMythicMobsHook().isMythicMob(entity) && cfg.isShowMythicName()) {
            return plugin.getMythicMobsHook().getMythicMobName(entity);
        }
        return formatTypeName(entity.getType().name());
    }

    private String buildBar(double current, double max, MobTagConfig cfg) {
        int length = cfg.getBarLength();
        double ratio = Math.min(1.0, current / max);
        int filled = (int) Math.round(ratio * length);
        int empty = length - filled;

        String filledColorStr = cfg.getFilledColor();
        if ("gradient".equalsIgnoreCase(filledColorStr)) {
            double pct = ratio * 100;
            if (pct > 60) filledColorStr = cfg.getGradientHigh();
            else if (pct > 30) filledColorStr = cfg.getGradientMedium();
            else filledColorStr = cfg.getGradientLow();
        }

        String emptyColorStr = cfg.getEmptyColor();
        StringBuilder bar = new StringBuilder();
        bar.append(colorize(filledColorStr));
        bar.append(cfg.getFilledChar().repeat(Math.max(0, filled)));
        bar.append(colorize(emptyColorStr));
        bar.append(cfg.getEmptyChar().repeat(Math.max(0, empty)));

        return bar.toString();
    }

    private String formatTypeName(String type) {
        String[] words = type.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1))
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }

    public static String colorize(String text) {
        if (text == null) return "";
        // Hex colors &#RRGGBB
        text = text.replaceAll("&#([A-Fa-f0-9]{6})", "\u00A7x\u00A7$1"
                .replace("$1", ""));
        // Manual hex parsing
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            if (i + 7 < text.length() && text.charAt(i) == '&' && text.charAt(i + 1) == '#') {
                String hex = text.substring(i + 2, i + 8);
                if (hex.matches("[0-9A-Fa-f]{6}")) {
                    sb.append('\u00A7').append('x');
                    for (char c : hex.toCharArray()) {
                        sb.append('\u00A7').append(c);
                    }
                    i += 8;
                    continue;
                }
            }
            sb.append(text.charAt(i));
            i++;
        }
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    public void removeTag(UUID uid) {
        ArmorStand stand = tags.remove(uid);
        if (stand != null && !stand.isDead()) {
            stand.remove();
        }
    }

    public void removeAllTags() {
        for (ArmorStand stand : tags.values()) {
            if (!stand.isDead()) stand.remove();
        }
        tags.clear();
        lastDamager.clear();
        if (task != null) task.cancel();
    }

    public void setLastDamager(UUID mobUID, UUID playerUID) {
        lastDamager.put(mobUID, playerUID);
    }

    public Map<UUID, ArmorStand> getTags() {
        return tags;
    }
}
