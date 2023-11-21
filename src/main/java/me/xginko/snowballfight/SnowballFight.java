package me.xginko.snowballfight;

import com.tcoded.folialib.FoliaLib;
import me.xginko.snowballfight.commands.snowballs.SnowballsCommand;
import me.xginko.snowballfight.modules.SnowballModule;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class SnowballFight extends JavaPlugin {

    private static SnowballFight instance;
    private static SnowballCache cache;
    private static SnowballConfig config;
    private static Logger logger;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        logger.info("                            ");
        logger.info("           ██████           ");
        logger.info("         ██████████         ");
        logger.info("        ████████████        ");
        logger.info("         ██████████         ");
        logger.info("           ██████           ");
        logger.info("                            ");
        logger.info("       Snowball Fight       ");
        logger.info("       Made by xGinko       ");
        logger.info("                            ");
        logger.info("Loading Config");
        reloadConfiguration();
        logger.info("Registering Commands");
        getCommand("snowballs").setExecutor(new SnowballsCommand());
        logger.info("Done.");
    }

    public void reloadConfiguration() {
        try {
            config = new SnowballConfig();
            cache = new SnowballCache(config.cacheKeepSeconds);
            SnowballModule.reloadModules();
            config.saveConfig();
        } catch (Exception e) {
            logger.severe("Error loading config! - " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public static SnowballFight getInstance() {
        return instance;
    }
    public static SnowballCache getCache() {
        return cache;
    }
    public static FoliaLib getFoliaLib() {
        return new FoliaLib(instance);
    }
    public static SnowballConfig getConfiguration() {
        return config;
    }
    public static Logger getLog() {
        return logger;
    }
}