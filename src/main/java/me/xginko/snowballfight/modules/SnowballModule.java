package me.xginko.snowballfight.modules;

import com.google.common.collect.ImmutableSet;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.utils.Disableable;
import me.xginko.snowballfight.utils.Enableable;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import space.arim.morepaperlib.scheduling.GracefulScheduling;

import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class SnowballModule implements Enableable, Disableable {

    protected static final Set<Class<SnowballModule>> AVAILABLE_MODULES;
    protected static final Set<SnowballModule> ENABLED_MODULES;

    static {
        // Disable reflection logging for this operation because its just confusing and provides no value.
        Configurator.setLevel(SnowballFight.class.getPackage().getName() + ".libs.reflections.Reflections", Level.OFF);
        AVAILABLE_MODULES = new Reflections(SnowballModule.class.getPackage().getName())
                .get(Scanners.SubTypes.of(SnowballModule.class).asClass())
                .stream()
                .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()))
                .map(clazz -> (Class<SnowballModule>) clazz)
                .sorted(Comparator.comparing(Class::getSimpleName))
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableSet::copyOf));
        ENABLED_MODULES = new HashSet<>();
    }

    protected final SnowballFight plugin;
    protected final SnowballConfig config;
    protected final GracefulScheduling scheduling;
    protected final String configPath, logFormat;
    protected final boolean enabled_in_config;

    public SnowballModule(String configPath, boolean defEnabled) {
        this(configPath, defEnabled, null);
    }

    public SnowballModule(String configPath, boolean defEnabled, String comment) {
        this.configPath = configPath;
        this.plugin = SnowballFight.getInstance();
        this.config = SnowballFight.config();
        this.scheduling = SnowballFight.scheduling();

        this.enabled_in_config = config.getBoolean(configPath + ".enable", defEnabled);
        if (comment != null) config.master().addComment(configPath, comment);

        String[] paths = configPath.split("\\.");
        if (paths.length <= 2) {
            this.logFormat = "<" + configPath + "> {}";
        } else {
            this.logFormat = "<" + paths[paths.length - 2] + "." + paths[paths.length - 1] + "> {}";
        }
    }

    public boolean shouldEnable() {
        return enabled_in_config;
    }

    public static void disableAll() {
        ENABLED_MODULES.forEach(Disableable::disable);
        ENABLED_MODULES.clear();
    }

    public static void reloadModules() {
        disableAll();

        for (Class<SnowballModule> moduleClass : AVAILABLE_MODULES) {
            try {
                SnowballModule module = moduleClass.getDeclaredConstructor().newInstance();
                if (module.shouldEnable()) {
                    ENABLED_MODULES.add(module);
                }
            } catch (Throwable throwable) {
                SnowballFight.logger().warn("Failed initialising module class '{}'.", moduleClass.getSimpleName(), throwable);
            }
        }

        ENABLED_MODULES.forEach(Enableable::enable);
    }

    protected void error(String message, Throwable throwable) {
        SnowballFight.logger().error(logFormat, message, throwable);
    }

    protected void error(String message) {
        SnowballFight.logger().error(logFormat, message);
    }

    protected void warn(String message) {
        SnowballFight.logger().warn(logFormat, message);
    }

    protected void info(String message) {
        SnowballFight.logger().info(logFormat, message);
    }

    protected void notRecognized(Class<?> clazz, String unrecognized) {
        warn("Unable to parse " + clazz.getSimpleName() + " at '" + unrecognized + "'. Please check your configuration.");
    }
}
