package me.abood8001.mobtag.managers;

import me.abood8001.mobtag.MobTag;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ToggleManager {

    private final MobTag plugin;
    private final File file;
    private FileConfiguration data;
    private final Set<UUID> hiddenPlayers = new HashSet<>();

    public ToggleManager(MobTag plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "toggles.yml");
        load();
    }

    public void load() {
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        data = YamlConfiguration.loadConfiguration(file);
        hiddenPlayers.clear();
        if (data.isList("hidden")) {
            for (String uuid : data.getStringList("hidden")) {
                try { hiddenPlayers.add(UUID.fromString(uuid)); } catch (Exception ignored) {}
            }
        }
    }

    public void save() {
        data.set("hidden", hiddenPlayers.stream().map(UUID::toString).toList());
        try { data.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public boolean isHidden(UUID uuid) {
        return hiddenPlayers.contains(uuid);
    }

    public boolean toggle(UUID uuid) {
        if (hiddenPlayers.contains(uuid)) {
            hiddenPlayers.remove(uuid);
            save();
            return false;
        } else {
            hiddenPlayers.add(uuid);
            save();
            return true;
        }
    }
}