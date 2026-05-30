package me.abood8001.mobtag;

import me.abood8001.mobtag.commands.MobTagCommand;
import me.abood8001.mobtag.hooks.MythicMobsHook;
import me.abood8001.mobtag.hooks.PAPIHook;
import org.bukkit.plugin.java.JavaPlugin;

public class MobTag extends JavaPlugin {

    private static MobTag instance;
    private MobTagManager tagManager;
    private MobTagConfig mobTagConfig;
    private MythicMobsHook mythicMobsHook;
    private boolean papiEnabled = false;
    private boolean mythicMobsEnabled = false;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();
        this.mobTagConfig = new MobTagConfig(this);

        // Setup hooks
        setupHooks();

        // Setup manager & listeners
        this.tagManager = new MobTagManager(this);
        getServer().getPluginManager().registerEvents(new MobTagListener(this), this);

        // Register command
        MobTagCommand cmd = new MobTagCommand(this);
        getCommand("mobtag").setExecutor(cmd);
        getCommand("mobtag").setTabCompleter(cmd);

        // Start update task
        tagManager.startTask();

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
        // PlaceholderAPI
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            if (mobTagConfig.isPapiEnabled()) {
                new PAPIHook(this).register();
                papiEnabled = true;
                getLogger().info("PlaceholderAPI hook enabled.");
            }
        }

        // MythicMobs
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
        if (tagManager != null) {
            tagManager.removeAllTags();
            tagManager.reload();
        }
    }

    // ── Getters ──────────────────────────────────────────────

    public static MobTag getInstance() {
        return instance;
    }

    public MobTagManager getTagManager() {
        return tagManager;
    }

    public MobTagConfig getMobTagConfig() {
        return mobTagConfig;
    }

    public MythicMobsHook getMythicMobsHook() {
        return mythicMobsHook;
    }

    public boolean isPapiEnabled() {
        return papiEnabled;
    }

    public boolean isMythicMobsEnabled() {
        return mythicMobsEnabled;
    }
}
