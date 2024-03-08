package me.xginko.snowballfight;

import io.github.thatsmusic99.configurationmaster.api.ConfigFile;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class SnowballConfig {

    private final @NotNull ConfigFile configFile;
    public final @NotNull List<Color> colors;
    public final @NotNull Duration cacheDuration;

    SnowballConfig() throws Exception {
        // Create plugin folder first if it does not exist yet
        File pluginFolder = SnowballFight.getInstance().getDataFolder();
        if (!pluginFolder.exists() && !pluginFolder.mkdir())
            SnowballFight.getLog().error("Failed to create plugin folder.");
        // Load config.yml with ConfigMaster
        this.configFile = ConfigFile.loadConfig(new File(pluginFolder, "config.yml"));

        this.cacheDuration = Duration.ofSeconds(getInt("settings.cache-keep-seconds", 20,
                "Don't touch unless you know what you're doing."));

        final List<String> defaults = Arrays.asList(
                "B3E3F4",   // Snowy Dark Sky
                "B5E5E7",   // Early Winter Snow White slightly Blue
                "71C3DB",   // Wet Snow Blue White
                "9BDBFF",   // Midwinter White slightly more Blue
                "E8EBF0",   // Frost on thin twigs White
                "59B1BD",   // Midday snow shadow blue
                "407794"    // Evening slightly red sun snow shadow but more blue
        );
        this.colors = getList("settings.colors", defaults,
                "You need to configure at least 1 color. Format: 'B3E3F4' or '#B3E3F4'")
                .stream()
                .distinct()
                .map(hexString -> {
                    try {
                        final String parseable = hexString.replace("#", "");
                        return Color.fromRGB(
                                Integer.parseInt(parseable.substring(0, 2), 16),
                                Integer.parseInt(parseable.substring(2, 4), 16),
                                Integer.parseInt(parseable.substring(4, 6), 16)
                        );
                    } catch (NumberFormatException e) {
                        SnowballFight.getLog().warn("Could not parse color '" + hexString + "'. Is it formatted correctly?");
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (colors.isEmpty()) {
            colors.addAll(defaults.stream()
                    .map(string -> {
                        try {
                            return Color.fromRGB(
                                    Integer.parseInt(string.substring(0, 2), 16),
                                    Integer.parseInt(string.substring(2, 4), 16),
                                    Integer.parseInt(string.substring(4, 6), 16));
                        } catch (Exception e) {
                            return Color.WHITE;
                        }
                    })
                    .distinct()
                    .collect(Collectors.toList()));
        }

        structure();
    }

    public void structure() {
        configFile.addDefault("settings.cooldown", null);
        configFile.addDefault("settings.infinite-snowballs", null);
        configFile.addDefault("settings.damage", null);
        configFile.addDefault("settings.snow", null);
        configFile.addDefault("settings.explosions", null);
        configFile.addDefault("settings.trails", null);
        configFile.addDefault("settings.fireworks", null);
        configFile.addDefault("settings.lightning", null);
        configFile.addDefault("settings.drop-armor", null);
        configFile.addDefault("settings.slowness", null);
        configFile.addDefault("settings.levitation", null);
    }

    public void saveConfig() {
        try {
            configFile.save();
        } catch (Exception e) {
            SnowballFight.getLog().error("Failed to save config file!", e);
        }
    }

    public @NotNull ConfigFile master() {
        return configFile;
    }

    public boolean getBoolean(@NotNull String path, boolean def) {
        configFile.addDefault(path, def);
        return configFile.getBoolean(path, def);
    }

    public boolean getBoolean(@NotNull String path, boolean def, @NotNull String comment) {
        configFile.addDefault(path, def, comment);
        return configFile.getBoolean(path, def);
    }

    public int getInt(@NotNull String path, int def) {
        configFile.addDefault(path, def);
        return configFile.getInteger(path, def);
    }

    public int getInt(@NotNull String path, int def, @NotNull String comment) {
        configFile.addDefault(path, def, comment);
        return configFile.getInteger(path, def);
    }

    public float getFloat(@NotNull String path, float def) {
        configFile.addDefault(path, def);
        return configFile.getFloat(path, def);
    }

    public float getFloat(@NotNull String path, float def, @NotNull String comment) {
        configFile.addDefault(path, def, comment);
        return configFile.getFloat(path, def);
    }

    public double getDouble(@NotNull String path, double def) {
        configFile.addDefault(path, def);
        return configFile.getDouble(path, def);
    }

    public double getDouble(@NotNull String path, double def, @NotNull String comment) {
        configFile.addDefault(path, def, comment);
        return configFile.getDouble(path, def);
    }

    public @NotNull String getString(@NotNull String path, @NotNull String def) {
        configFile.addDefault(path, def);
        return configFile.getString(path, def);
    }

    public @NotNull String getString(@NotNull String path, @NotNull String def, @NotNull String comment) {
        configFile.addDefault(path, def, comment);
        return configFile.getString(path, def);
    }

    public @NotNull List<String> getList(@NotNull String path, @NotNull List<String> def) {
        configFile.addDefault(path, def);
        return configFile.getStringList(path);
    }

    public @NotNull List<String> getList(@NotNull String path, @NotNull List<String> def, @NotNull String comment) {
        configFile.addDefault(path, def, comment);
        return configFile.getStringList(path);
    }
}