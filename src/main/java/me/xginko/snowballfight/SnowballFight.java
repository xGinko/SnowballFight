package me.xginko.snowballfight;

import com.tcoded.folialib.FoliaLib;
import me.xginko.snowballfight.commands.snowballs.SnowballsCommand;
import me.xginko.snowballfight.modules.SnowballModule;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public final class SnowballFight extends JavaPlugin {

    private static SnowballFight instance;
    private static SnowballCache cache;
    private static SnowballConfig config;
    private static BukkitAudiences audiences;
    private static FoliaLib foliaLib;
    private static ComponentLogger logger;
    private static Random random;
    private static Metrics metrics;

    @Override
    public void onEnable() {
        instance = this;
        audiences = BukkitAudiences.create(this);
        foliaLib = new FoliaLib(this);
        random = new Random();
        logger = ComponentLogger.logger(getLogger().getName());
        metrics = new Metrics(this, 21271);
        final Style snowy = Style.style().decorate(TextDecoration.BOLD).color(TextColor.color(181,229,231)).build();
        logger.info(Component.text("                            ").style(snowy));
        logger.info(Component.text("           ██████           ").style(snowy));
        logger.info(Component.text("         ██████████         ").style(snowy));
        logger.info(Component.text("        ████████████        ").style(snowy));
        logger.info(Component.text("         ██████████         ").style(snowy));
        logger.info(Component.text("           ██████           ").style(snowy));
        logger.info(Component.text("                            ").style(snowy));
        logger.info(Component.text("       Snowball Fight       ").style(snowy));
        logger.info(Component.text("       Made by xGinko       ").style(snowy));
        logger.info(Component.text("                            ").style(snowy));
        logger.info(Component.text("Loading Config"));
        reloadConfiguration();
        logger.info(Component.text("Registering Commands"));
        getCommand("snowballs").setExecutor(new SnowballsCommand());
        logger.info(Component.text("Done."));
    }

    @Override
    public void onDisable() {
        SnowballModule.modules.forEach(SnowballModule::disable);
        SnowballModule.modules.clear();
        if (foliaLib != null) {
            foliaLib.getImpl().cancelAllTasks();
            foliaLib = null;
        }
        if (cache != null) {
            cache.cacheMap().clear();
            cache = null;
        }
        if (audiences != null) {
            audiences.close();
            audiences = null;
        }
        if (metrics != null) {
            metrics.shutdown();
            metrics = null;
        }
        random = null;
        logger = null;
        config = null;
        instance = null;
    }

    public void reloadConfiguration() {
        try {
            config = new SnowballConfig();
            cache = new SnowballCache(config.cacheKeepSeconds);
            SnowballModule.reloadModules();
            config.saveConfig();
        } catch (Throwable e) {
            logger.error("Error loading config!", e);
        }
    }

    public static SnowballFight getInstance() {
        return instance;
    }
    public static SnowballCache getCache() {
        return cache;
    }
    public static BukkitAudiences getAudiences() {
        return audiences;
    }
    public static FoliaLib getFoliaLib() {
        return foliaLib;
    }
    public static SnowballConfig getConfiguration() {
        return config;
    }
    public static ComponentLogger getLog() {
        return logger;
    }
    public static Random getRandom() {
        return random;
    }
}