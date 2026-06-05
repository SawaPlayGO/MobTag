package me.abood8001.mobtag;

import me.abood8001.mobtag.commands.MobTagCommand;
import me.abood8001.mobtag.hooks.MythicMobsHook;
import me.abood8001.mobtag.hooks.PAPIHook;
import me.abood8001.mobtag.managers.ToggleManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class MobTag extends JavaPlugin {

    private static MobTag instance;
    private MobTagManager tagManager;
    private MobTagConfig mobTagConfig;
    private MythicMobsHook mythicMobsHook;
    private ToggleManager toggleManager;
    private boolean papiEnabled = false;
    private boolean mythicMobsEnabled = false;
    private boolean packetEventsEnabled = false;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        this.mobTagConfig = new MobTagConfig(this);
        this.toggleManager = new ToggleManager(this);

        setupHooks();
        this.tagManager = new MobTagManager(this);
        getServer().getPluginManager().registerEvents(new MobTagListener(this), this);

        MobTagCommand cmd = new MobTagCommand(this);
        getCommand("mobtag").setExecutor(cmd);
        getCommand("mobtag").setTabCompleter(cmd);

        tagManager.startTask();

        // bStats
        new Metrics(this, 25516);

        getLogger().info("MobTag v" + getDescription().getVersion() + " enabled! by Abood_8001");
    }

    @Override
    public void onDisable() {
        if (tagManager != null) {
            tagManager.removeAllTags();
        }
        getLogger().info("MobTag disabled. All tags removed.");
    }

    private void setupHooks() {
        if (getServer().getPluginManager().getPlugin("packetevents") != null ||
                getServer().getPluginManager().getPlugin("PacketEvents") != null) {
            packetEventsEnabled = true;
            getLogger().info("PacketEvents hook enabled - smooth tag movement active.");
        }
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            if (mobTagConfig.isPapiEnabled()) {
                new PAPIHook(this).register();
                papiEnabled = true;
                getLogger().info("PlaceholderAPI hook enabled.");
            }
        }

        if (getServer().getPluginManager().getPlugin("MythicMobs") != null) {
            if (mobTagConfig.isMythicMobsEnabled()) {
                mythicMobsHook = new MythicMobsHook(this);
                mythicMobsEnabled = true;
                getLogger().info("MythicMobs hook enabled.");
            }
        }
    }

    public void reload() {
        reloadConfig();
        this.mobTagConfig = new MobTagConfig(this);
        this.toggleManager = new ToggleManager(this);
        this.papiEnabled = false;
        this.mythicMobsEnabled = false;
        this.mythicMobsHook = null;
        setupHooks();
        if (tagManager != null) {
            tagManager.removeAllTags();
            tagManager.reload();
        }
    }

    public static MobTag getInstance() { return instance; }
    public MobTagManager getTagManager() { return tagManager; }
    public MobTagConfig getMobTagConfig() { return mobTagConfig; }
    public MythicMobsHook getMythicMobsHook() { return mythicMobsHook; }
    public ToggleManager getToggleManager() { return toggleManager; }
    public boolean isPapiEnabled() { return papiEnabled; }
    public boolean isMythicMobsEnabled() { return mythicMobsEnabled; }
    public boolean isPacketEventsEnabled() { return packetEventsEnabled; }
}