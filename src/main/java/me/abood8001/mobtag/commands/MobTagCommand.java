package me.abood8001.mobtag.commands;

import me.abood8001.mobtag.MobTag;
import me.abood8001.mobtag.MobTagManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

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

        if (!sender.hasPermission("mobtag.admin")) {
            sender.sendMessage(prefix + MobTagManager.colorize(plugin.getMobTagConfig().getMsgNoPermission()));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(prefix + MobTagManager.colorize("&fUsage: &e/mobtag reload"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reload();
            sender.sendMessage(prefix + MobTagManager.colorize(plugin.getMobTagConfig().getMsgReloadSuccess()));
        } else {
            sender.sendMessage(prefix + MobTagManager.colorize(plugin.getMobTagConfig().getMsgUnknownCommand()));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("mobtag.admin")) {
            return Collections.singletonList("reload");
        }
        return Collections.emptyList();
    }
}
