package me.xginko.snowballfight;

import io.github.thatsmusic99.configurationmaster.api.ConfigFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class SnowballConfig {

    private final @NotNull ConfigFile config;
    public final float explosionPower;
    public final boolean explosionSetFire, explosionBreakBlocks;

    protected SnowballConfig() throws Exception {
        this.config = loadConfig(new File(SnowballFight.getInstance().getDataFolder(), "config.yml"));
        this.explosionPower = getFloat("snowball-explosion.power", 6.0F,
                "TNT has a power of 4.0, end crystals 6.0");
        this.explosionSetFire = getBoolean("snowball-explosion.set-fire", false,
                "Enable explosion fire like with respawn anchors.");
        this.explosionBreakBlocks = getBoolean("snowball-explosion.break-blocks", true,
                "Enable destruction of nearby blocks.");
    }

    private ConfigFile loadConfig(File ymlFile) throws Exception {
        File parent = new File(ymlFile.getParent());
        if (!parent.exists() && !parent.mkdir()) SnowballFight.getLog().severe("Unable to create plugin config directory.");
        if (!ymlFile.exists()) ymlFile.createNewFile();
        return ConfigFile.loadConfig(ymlFile);
    }

    public void saveConfig() {
        try {
            config.save();
        } catch (Exception e) {
            SnowballFight.getLog().severe("Failed to save config file! - " + e.getLocalizedMessage());
        }
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