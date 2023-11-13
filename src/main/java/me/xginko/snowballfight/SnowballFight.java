package me.xginko.snowballfight;

import me.xginko.snowballfight.commands.snowballs.SnowballsCommand;
import me.xginko.snowballfight.modules.SnowballModule;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class SnowballFight extends JavaPlugin {

    private static SnowballFight instance;
    private static SnowballConfig config;
    private static Logger logger;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        reloadConfiguration();
        getCommand("snowballs").setExecutor(new SnowballsCommand());
    }

    public void reloadConfiguration() {
        try {
            config = new SnowballConfig();
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
    public static NamespacedKey getKey(String key) {
        return new NamespacedKey(instance, key);
    }
    public static SnowballConfig getConfiguration() {
        return config;
    }
    public static Logger getLog() {
        return logger;
    }
}
