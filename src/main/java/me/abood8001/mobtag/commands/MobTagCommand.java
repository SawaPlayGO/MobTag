package me.abood8001.mobtag.commands;

import me.abood8001.mobtag.MobTag;
import me.abood8001.mobtag.MobTagManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MobTagCommand implements CommandExecutor, TabCompleter {

    private final MobTag plugin;

    public MobTagCommand(MobTag plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = MobTagManager.colorize(plugin.getMobTagConfig().getPrefix());

        if (args.length == 0) {
            sender.sendMessage(prefix + MobTagManager.colorize("&fUsage: &e/mobtag reload&f or &e/mobtag toggle"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("mobtag.admin")) {
                sender.sendMessage(prefix + MobTagManager.colorize(plugin.getMobTagConfig().getMsgNoPermission()));
                return true;
            }
            plugin.reload();
            sender.sendMessage(prefix + MobTagManager.colorize(plugin.getMobTagConfig().getMsgReloadSuccess()));

        } else if (args[0].equalsIgnoreCase("toggle")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(prefix + MobTagManager.colorize("&cOnly players can use this command."));
                return true;
            }
            if (!player.hasPermission("mobtag.toggle")) {
                player.sendMessage(prefix + MobTagManager.colorize(plugin.getMobTagConfig().getMsgNoPermission()));
                return true;
            }
            boolean hidden = plugin.getToggleManager().toggle(player.getUniqueId());
            if (hidden) {
                player.sendMessage(prefix + MobTagManager.colorize("&cMob health tags &lhidden&r&c."));
            } else {
                player.sendMessage(prefix + MobTagManager.colorize("&aMob health tags &lvisible&r&a."));
            }

        } else {
            sender.sendMessage(prefix + MobTagManager.colorize(plugin.getMobTagConfig().getMsgUnknownCommand()));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            if (sender.hasPermission("mobtag.admin") && sender.hasPermission("mobtag.toggle")) {
                return Arrays.asList("reload", "toggle");
            } else if (sender.hasPermission("mobtag.admin")) {
                return Collections.singletonList("reload");
            } else if (sender.hasPermission("mobtag.toggle")) {
                return Collections.singletonList("toggle");
            }
        }
        return Collections.emptyList();
    }
}