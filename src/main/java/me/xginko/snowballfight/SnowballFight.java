package me.xginko.snowballfight;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.xginko.snowballfight.commands.snowballs.SnowballsCommand;
import me.xginko.snowballfight.modules.SnowballModule;
import me.xginko.snowballfight.utils.Util;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import space.arim.morepaperlib.MorePaperLib;
import space.arim.morepaperlib.scheduling.GracefulScheduling;

import java.util.Random;
import java.util.UUID;

public final class SnowballFight extends JavaPlugin {

    private static SnowballFight instance;
    private static Cache<UUID, WrappedSnowball> cache;
    private static SnowballConfig config;
    private static BukkitAudiences audiences;
    private static GracefulScheduling scheduling;
    private static ComponentLogger logger;
    private static Random random;
    private static Metrics metrics;
    private static boolean isServerFolia;

    @Override
    public void onEnable() {
        instance = this;
        random = new Random();
        audiences = BukkitAudiences.create(this);
        scheduling = new MorePaperLib(instance).scheduling();
        logger = ComponentLogger.logger(getLogger().getName());
        metrics = new Metrics(this, 21271);

        Style snowy = Style.style().decorate(TextDecoration.BOLD).color(TextColor.color(181,229,231)).build();
        logger.info(Component.text("                            ").style(snowy));
        logger.info(Component.text("           ██████           ").style(snowy));
        logger.info(Component.text("         ██████████         ").style(snowy));
        logger.info(Component.text("        ████████████        ").style(snowy));
        logger.info(Component.text("         ██████████         ").style(snowy));
        logger.info(Component.text("           ██████           ").style(snowy));
        logger.info(Component.text("                            ").style(snowy));
        logger.info(Component.text("       Snowball Fight       ").style(snowy));
        logger.info(Component.text("         by xGinko          ").style(snowy));
        logger.info(Component.text("                            ").style(snowy));

        isServerFolia = Util.hasClass("io.papermc.paper.threadedregions.RegionizedServer");
        if (isServerFolia) logger.info("Detected Folia server.");

        logger.info(Component.text("Loading Config"));
        reloadConfiguration();

        logger.info(Component.text("Registering Commands"));
        getCommand("snowballs").setExecutor(new SnowballsCommand());

        logger.info(Component.text("Done."));
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
        instance = null;
        random = null;
        logger = null;
        config = null;
        cache = null;
    }

    public void disableRunningTasks() {
        SnowballModule.disableAll();
        if (scheduling != null) scheduling.cancelGlobalTasks();
        if (cache != null) cache.cleanUp();
    }

    public void reloadConfiguration() {
        try {
            disableRunningTasks();
            config = new SnowballConfig();
            cache = Caffeine.newBuilder().expireAfterWrite(config.cacheDuration).build();
            SnowballModule.reloadModules();
            config.saveConfig();
        } catch (Throwable e) {
            logger.error("Error loading config!", e);
        }
    }

    public static SnowballFight getInstance() {
        return instance;
    }

    public static Cache<UUID, WrappedSnowball> getCache() {
        return cache;
    }

    public static BukkitAudiences getAudiences() {
        return audiences;
    }

    public static GracefulScheduling getScheduler() {
        return scheduling;
    }

    public static boolean isServerFolia() {
        return isServerFolia;
    }

    public static SnowballConfig config() {
        return config;
    }

    public static ComponentLogger logger() {
        return logger;
    }

    public static Random getRandom() {
        return random;
    }
}