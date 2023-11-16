package me.xginko.snowballfight.models;

import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import org.bukkit.Color;
import org.bukkit.entity.Snowball;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WrappedSnowball {

    private final @NotNull Snowball snowball;
    private @Nullable Color primaryColor, secondaryColor;

    public WrappedSnowball(@NotNull Snowball snowball) {
        this.snowball = snowball;
    }

    public @NotNull Snowball snowball() {
        return snowball;
    }

    public @NotNull Color getPrimaryColor() {
        SnowballConfig config = SnowballFight.getConfiguration();
        if (primaryColor == null)
            primaryColor = config.getRandomColor();
        if (secondaryColor == null)
            return primaryColor;
        int tries = 0;
        while (primaryColor.equals(secondaryColor)) {
            if (tries > 100) break;
            primaryColor = config.getRandomColor();
            tries++;
        }
        return primaryColor;
    }

    public @NotNull Color getSecondaryColor() {
        SnowballConfig config = SnowballFight.getConfiguration();
        if (secondaryColor == null)
            secondaryColor = config.getRandomColor();
        if (primaryColor == null)
            return secondaryColor;
        int tries = 0;
        while (secondaryColor.equals(primaryColor)) {
            if (tries > 100) break;
            secondaryColor = config.getRandomColor();
            tries++;
        }
        return secondaryColor;
    }
}