package me.xginko.snowballfight;

import io.github.thatsmusic99.configurationmaster.api.ConfigFile;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnowballConfig {

    private final @NotNull ConfigFile config;
    public final @NotNull List<Color> colors = new ArrayList<>();
    public final int cacheKeepSeconds;

    protected SnowballConfig() throws Exception {
        this.config = loadConfig(new File(SnowballFight.getInstance().getDataFolder(), "config.yml"));
        this.cacheKeepSeconds = getInt("settings.cache-keep-seconds", 20, "\nDon't touch unless you know what you're doing.");
        final MiniMessage miniMessage = MiniMessage.miniMessage();
        final List<String> defaults = List.of(
                "<color:#B3E3F4>",   // Snowy Dark Sky
                "<color:#B5E5E7>",   // Early Winter Snow White slightly Blue
                "<color:#71C3DB>",   // Wet Snow Blue White
                "<color:#9BDBFF>",   // Mid Winter White slightly more Blue
                "<color:#E8EBF0>",   // Frost on thin twigs White
                "<color:#59B1BD>",   // Mid day snow shadow blue
                "<color:#407794>"    // Evening slightly red sun snow shadow but more blue
        );
        List<String> configuredColors = getList("settings.colors", defaults, "\nYou need to configure at least 2 colors.");
        if (configuredColors.size() < 2) {
            SnowballFight.getLog().severe("You need to configure at least 2 colors. Resetting to default colors.");
            config.set("settings.colors", defaults);
            configuredColors = defaults; // Simple workaround as the new config list needs to be updated again after config.set to not stay invalid.
        }
        configuredColors.forEach(serializedColor -> {
            final TextColor textColor = miniMessage.deserialize(serializedColor).color();
            if (textColor != null) this.colors.add(Color.fromRGB(textColor.red(), textColor.green(), textColor.blue()));
            else SnowballFight.getLog().warning("Color '" + serializedColor + "' is not formatted properly. Use the following format: <color:#FFFFFF>");
        });
        structure();
    }

    private ConfigFile loadConfig(File ymlFile) throws Exception {
        File parent = new File(ymlFile.getParent());
        if (!parent.exists() && !parent.mkdir()) SnowballFight.getLog().severe("Unable to create plugin config directory.");
        if (!ymlFile.exists()) ymlFile.createNewFile();
        return ConfigFile.loadConfig(ymlFile);
    }

    public void structure() {
        config.addDefault("settings.cooldown", null);
        config.addDefault("settings.damage", null);
        config.addDefault("settings.explosions", null);
        config.addDefault("settings.fireworks", null);
        config.addDefault("settings.trails", null);
        config.addDefault("settings.lightning", null);
        config.addDefault("settings.levitation", null);
        config.addDefault("settings.snow", null);
    }

    public void saveConfig() {
        try {
            config.save();
        } catch (Exception e) {
            SnowballFight.getLog().severe("Failed to save config file! - " + e.getLocalizedMessage());
        }
    }

    public Color getRandomColor() {
        return this.colors.get(new Random().nextInt(this.colors.size()));
    }

    public @NotNull ConfigFile master() {
        return config;
    }

    public boolean getBoolean(@NotNull String path, boolean def) {
        config.addDefault(path, def);
        return config.getBoolean(path, def);
    }

    public boolean getBoolean(@NotNull String path, boolean def, @NotNull String comment) {
        config.addDefault(path, def, comment);
        return config.getBoolean(path, def);
    }

    public int getInt(@NotNull String path, int def) {
        config.addDefault(path, def);
        return config.getInteger(path, def);
    }

    public int getInt(@NotNull String path, int def, @NotNull String comment) {
        config.addDefault(path, def, comment);
        return config.getInteger(path, def);
    }

    public float getFloat(@NotNull String path, float def) {
        config.addDefault(path, def);
        return config.getFloat(path, def);
    }

    public float getFloat(@NotNull String path, float def, @NotNull String comment) {
        config.addDefault(path, def, comment);
        return config.getFloat(path, def);
    }

    public double getDouble(@NotNull String path, double def) {
        config.addDefault(path, def);
        return config.getDouble(path, def);
    }

    public double getDouble(@NotNull String path, double def, @NotNull String comment) {
        config.addDefault(path, def, comment);
        return config.getDouble(path, def);
    }

    public @NotNull String getString(@NotNull String path, @NotNull String def) {
        config.addDefault(path, def);
        return config.getString(path, def);
    }

    public @NotNull String getString(@NotNull String path, @NotNull String def, @NotNull String comment) {
        config.addDefault(path, def, comment);
        return config.getString(path, def);
    }

    public @NotNull List<String> getList(@NotNull String path, @NotNull List<String> def) {
        config.addDefault(path, def);
        return config.getStringList(path);
    }

    public @NotNull List<String> getList(@NotNull String path, @NotNull List<String> def, @NotNull String comment) {
        config.addDefault(path, def, comment);
        return config.getStringList(path);
    }
}