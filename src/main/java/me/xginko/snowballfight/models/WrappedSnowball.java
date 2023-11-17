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
        for (int i = 0; i < 100; i++) {
            if (!primaryColor.equals(secondaryColor)) break;
            else primaryColor = config.getRandomColor();
        }
        return primaryColor;
    }

    public @NotNull Color getSecondaryColor() {
        SnowballConfig config = SnowballFight.getConfiguration();
        if (secondaryColor == null)
            secondaryColor = config.getRandomColor();
        if (primaryColor == null)
            return secondaryColor;
        for (int i = 0; i < 100; i++) {
            if (!secondaryColor.equals(primaryColor)) break;
            else secondaryColor = config.getRandomColor();
        }
        return secondaryColor;
    }
}