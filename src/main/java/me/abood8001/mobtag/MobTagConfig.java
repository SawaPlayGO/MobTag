package me.abood8001.mobtag;

import org.bukkit.configuration.file.FileConfiguration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MobTagConfig {

    private final MobTag plugin;

    private int updateInterval;
    private double displayRange;
    private double tagHeightOffset;
    private String visibilityMode;
    private boolean ignorePassive;
    private boolean ignorePlayers;
    private boolean showOnBosses;
    private String tagFormat;

    // Health bar
    private int barLength;
    private String filledChar;
    private String emptyChar;
    private String filledColor;
    private String gradientHigh;
    private String gradientMedium;
    private String gradientLow;
    private String emptyColor;

    // MythicMobs
    private boolean mythicMobsEnabled;
    private boolean showMythicName;
    private String mythicCustomFormat;

    // PAPI
    private boolean papiEnabled;

    // Messages
    private String prefix;
    private String msgReloadSuccess;
    private String msgNoPermission;
    private String msgUnknownCommand;

    private List<String> blacklistedWorlds;
    private Map<String, String> entityFormats;

    public MobTagConfig(MobTag plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        FileConfiguration cfg = plugin.getConfig();

        entityFormats = new HashMap<>();
        if (cfg.isConfigurationSection("entity-formats")) {
            for (String key : cfg.getConfigurationSection("entity-formats").getKeys(false)) {
                entityFormats.put(key.toUpperCase(), cfg.getString("entity-formats." + key));
            }
        }
        blacklistedWorlds = cfg.getStringList("blacklisted-worlds");
        updateInterval    = cfg.getInt("update-interval", 4);
        displayRange      = cfg.getDouble("display-range", 16.0);
        tagHeightOffset   = cfg.getDouble("tag-height-offset", 0.3);
        visibilityMode    = cfg.getString("visibility-mode", "ALL").toUpperCase();
        ignorePassive     = cfg.getBoolean("ignore-passive", false);
        ignorePlayers     = cfg.getBoolean("ignore-players", true);
        showOnBosses      = cfg.getBoolean("show-on-bosses", true);
        tagFormat         = cfg.getString("tag-format", "&c❤ &f{current}&7/&f{max} {bar}");

        barLength         = cfg.getInt("health-bar.length", 10);
        filledChar        = cfg.getString("health-bar.filled-char", "█");
        emptyChar         = cfg.getString("health-bar.empty-char", "░");
        filledColor       = cfg.getString("health-bar.filled-color", "gradient");
        gradientHigh      = cfg.getString("health-bar.gradient-high", "&a");
        gradientMedium    = cfg.getString("health-bar.gradient-medium", "&e");
        gradientLow       = cfg.getString("health-bar.gradient-low", "&c");
        emptyColor        = cfg.getString("health-bar.empty-color", "&8");

        mythicMobsEnabled = cfg.getBoolean("mythicmobs.enabled", true);
        showMythicName    = cfg.getBoolean("mythicmobs.show-mythic-name", true);
        mythicCustomFormat= cfg.getString("mythicmobs.custom-format", "");

        papiEnabled       = cfg.getBoolean("placeholderapi.enabled", true);

        prefix            = cfg.getString("messages.prefix", "&8[&bMobTag&8] ");
        msgReloadSuccess  = cfg.getString("messages.reload-success", "&aConfiguration reloaded successfully!");
        msgNoPermission   = cfg.getString("messages.no-permission", "&cYou don't have permission to do that.");
        msgUnknownCommand = cfg.getString("messages.unknown-command", "&cUnknown sub-command. Usage: /mobtag reload");
    }

    // ── Getters ──────────────────────────────────────────────

    public Map<String, String> getEntityFormats() { return entityFormats; }
    public List<String> getBlacklistedWorlds() { return blacklistedWorlds; }
    public int getUpdateInterval() { return updateInterval; }
    public double getDisplayRange() { return displayRange; }
    public double getTagHeightOffset() { return tagHeightOffset; }
    public String getVisibilityMode() { return visibilityMode; }
    public boolean isIgnorePassive() { return ignorePassive; }
    public boolean isIgnorePlayers() { return ignorePlayers; }
    public boolean isShowOnBosses() { return showOnBosses; }
    public String getTagFormat() { return tagFormat; }

    public int getBarLength() { return barLength; }
    public String getFilledChar() { return filledChar; }
    public String getEmptyChar() { return emptyChar; }
    public String getFilledColor() { return filledColor; }
    public String getGradientHigh() { return gradientHigh; }
    public String getGradientMedium() { return gradientMedium; }
    public String getGradientLow() { return gradientLow; }
    public String getEmptyColor() { return emptyColor; }

    public boolean isMythicMobsEnabled() { return mythicMobsEnabled; }
    public boolean isShowMythicName() { return showMythicName; }
    public String getMythicCustomFormat() { return mythicCustomFormat; }

    public boolean isPapiEnabled() { return papiEnabled; }

    public String getPrefix() { return prefix; }
    public String getMsgReloadSuccess() { return msgReloadSuccess; }
    public String getMsgNoPermission() { return msgNoPermission; }
    public String getMsgUnknownCommand() { return msgUnknownCommand; }
}
