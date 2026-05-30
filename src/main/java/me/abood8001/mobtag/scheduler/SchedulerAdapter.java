package me.abood8001.mobtag.scheduler;

import me.abood8001.mobtag.MobTag;
import org.bukkit.Bukkit;

public class SchedulerAdapter {

    public static void runTaskTimer(MobTag plugin, Runnable task, long delay, long period) {
        Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
    }

    public static void cancelAllTasks(MobTag plugin) {
        Bukkit.getScheduler().cancelTasks(plugin);
    }
}