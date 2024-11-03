package me.xginko.snowballfight;

import me.xginko.snowballfight.commands.snowballs.SnowballsCommand;
import me.xginko.snowballfight.modules.SnowballModule;
import me.xginko.snowballfight.utils.Util;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import space.arim.morepaperlib.MorePaperLib;
import space.arim.morepaperlib.scheduling.GracefulScheduling;

import java.util.Objects;

public final class SnowballFight extends JavaPlugin {

    private static SnowballFight instance;
    private static SnowballTracker snowballs;
    private static SnowballConfig config;
    private static BukkitAudiences audiences;
    private static GracefulScheduling scheduling;
    private static ComponentLogger logger;
    private static Metrics metrics;
    private static boolean isServerFolia;

    @Override
    public void onEnable() {
        instance = this;
        audiences = BukkitAudiences.create(instance);
        scheduling = new MorePaperLib(instance).scheduling();
        logger = ComponentLogger.logger(getLogger().getName());
        metrics = new Metrics(instance, 21271);
        isServerFolia = Util.hasClass("io.papermc.paper.threadedregions.RegionizedServer");

        logger.info(Component.text("                            ").style(Util.SNOWY_WHITE_BOLD));
        logger.info(Component.text("           ██████           ").style(Util.SNOWY_WHITE_BOLD));
        logger.info(Component.text("         ██████████         ").style(Util.SNOWY_WHITE_BOLD));
        logger.info(Component.text("        ████████████        ").style(Util.SNOWY_WHITE_BOLD));
        logger.info(Component.text("         ██████████         ").style(Util.SNOWY_WHITE_BOLD));
        logger.info(Component.text("           ██████           ").style(Util.SNOWY_WHITE_BOLD));
        logger.info(Component.text("                            ").style(Util.SNOWY_WHITE_BOLD));
        logger.info(Component.text("       Snowball Fight       ").style(Util.SNOWY_WHITE_BOLD));
        logger.info(Component.text("         by xGinko          ").style(Util.SNOWY_WHITE_BOLD));
        logger.info(Component.text("                            ").style(Util.SNOWY_WHITE_BOLD));

        reloadConfiguration();

        Objects.requireNonNull(getCommand("snowballs"), "Command isn't defined in the plugin.yml!")
                .setExecutor(new SnowballsCommand());
    }

    @Override
    public void onDisable() {
        disableRunningTasks();
        if (audiences != null) {
            audiences.close();
            audiences = null;
        }
        if (metrics != null) {
            metrics.shutdown();
            metrics = null;
        }
        scheduling = null;
        snowballs = null;
        instance = null;
        logger = null;
        config = null;
    }

    public void disableRunningTasks() {
        SnowballModule.disableAll();
        if (scheduling != null) scheduling.cancelGlobalTasks();
        if (snowballs != null) snowballs.disable();
    }

    public void reloadConfiguration() {
        try {
            disableRunningTasks();
            config = new SnowballConfig();
            snowballs = new SnowballTracker(instance, config.snowballCacheDuration);
            SnowballModule.reloadModules();
            config.saveConfig();
        } catch (Throwable e) {
            logger.error("Error loading config!", e);
        }
    }

    public static SnowballFight getInstance() {
        return instance;
    }

    public static SnowballTracker snowballs() {
        return snowballs;
    }

    public static BukkitAudiences audiences() {
        return audiences;
    }

    public static GracefulScheduling scheduling() {
        return scheduling;
    }

    public static SnowballConfig config() {
        return config;
    }

    public static ComponentLogger logger() {
        return logger;
    }

    public static boolean isServerFolia() {
        return isServerFolia;
    }
}