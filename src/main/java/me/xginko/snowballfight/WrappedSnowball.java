package me.xginko.snowballfight;

import org.bukkit.Color;
import org.bukkit.entity.Snowball;
import org.jetbrains.annotations.NotNull;

public final class WrappedSnowball {

    private final @NotNull Snowball snowball;
    private @NotNull Color primaryColor, secondaryColor;

    public WrappedSnowball(@NotNull Snowball snowball) {
        this.snowball = snowball;
        SnowballConfig config = SnowballFight.config();
        if (config.colors.size() == 1) {
            this.primaryColor = this.secondaryColor = config.colors.get(0);
        } else {
            this.primaryColor = config.colors.get(SnowballFight.getRandom().nextInt(config.colors.size()));
            do {
                this.secondaryColor = config.colors.get(SnowballFight.getRandom().nextInt(config.colors.size()));
            } while (primaryColor == secondaryColor);
        }
    }

    public @NotNull Snowball snowball() {
        return snowball;
    }

    public @NotNull Color getPrimaryColor() {
        return primaryColor;
    }

    public @NotNull Color getSecondaryColor() {
        return secondaryColor;
    }

    public void setPrimaryColor(@NotNull Color primaryColor) {
        this.primaryColor = primaryColor;
    }

    public void setSecondaryColor(@NotNull Color secondaryColor) {
        this.secondaryColor = secondaryColor;
    }
}