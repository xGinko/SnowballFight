package me.xginko.snowballfight.modules.effects;

import me.xginko.pumpkinpvpreloaded.PumpkinPVPConfig;
import me.xginko.pumpkinpvpreloaded.PumpkinPVPReloaded;
import me.xginko.pumpkinpvpreloaded.events.PostPumpkinExplodeEvent;
import me.xginko.pumpkinpvpreloaded.events.PostPumpkinHeadEntityExplodeEvent;
import me.xginko.pumpkinpvpreloaded.modules.PumpkinPVPModule;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.modules.SnowballModule;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.*;

public class FireworkEffects implements SnowballModule, Listener {

    private final List<FireworkEffect> fireWorkEffects = new ArrayList<>();
    private boolean has_enough_colors = true;

    public FireworkEffects() {
        shouldEnable();
        SnowballConfig config = SnowballFight.getConfiguration();
        List<Color> parsedColors = new ArrayList<>();
        List<String> configuredColors = config.getList("firework-effects.colors", List.of(
                "FFAE03",   // Pumpkin Light Orange
                "FE4E00",   // Pumpkin Dark Orange
                "1A090D",   // Witch Hat Dark Purple
                "A42CD6",   // Witch Dress Pale Purple
                "A3EB1E"    // Slime Green
        ), "You need to configure at least 2 colors.");
        if (configuredColors.size() < 2) {
            SnowballFight.getLog().severe("You need to configure at least 2 colors. Disabling firework effects.");
            has_enough_colors = false;
        }
        configuredColors.forEach(hexString -> {
            try {
                int rgb = HexFormat.fromHexDigits(hexString);
                parsedColors.add(Color.fromRGB(rgb));
            } catch (IllegalArgumentException e) {
                SnowballFight.getLog().warning("Hex color string '"+hexString+"' is not formatted correctly. " +
                        "Try using the format without a prefix: eg. FFAE03 instead of #FFAE03 or 0xFFAE03");
            }
        });
        final boolean flicker = config.getBoolean("firework-effects.flicker", false);
        final boolean trail = config.getBoolean("firework-effects.trail", false);
        config.getList("firework-effects.types",
                Arrays.stream(FireworkEffect.Type.values()).map(Enum::name).toList(),
                """
                        FireworkEffect Types you wish to use. Has to be a valid enum from:
                        https://jd.papermc.io/paper/1.20/org/bukkit/FireworkEffect.Type.html
                        """
        ).forEach(effect -> {
            try {
                FireworkEffect.Type effectType = FireworkEffect.Type.valueOf(effect);
                parsedColors.forEach(primary_color -> {
                    Color secondary_color = primary_color;
                    int tries = 0;
                    while (secondary_color.equals(primary_color)) { // Avoid rolling the same color
                        if (tries > 100) break; // Avoid infinite loop on bad config
                        secondary_color = parsedColors.get(new Random().nextInt(0, parsedColors.size()));
                        tries++;
                    }
                    this.fireWorkEffects.add(FireworkEffect.builder()
                            .withColor(primary_color, secondary_color)
                            .with(effectType)
                            .flicker(flicker)
                            .trail(trail)
                            .build());
                });
            } catch (IllegalArgumentException e) {
                SnowballFight.getLog().warning("FireworkEffect Type '"+effect+"' not recognized. " +
                        "Please use valid enums from: https://jd.papermc.io/paper/1.20/org/bukkit/FireworkEffect.Type.html");
            }
        });
    }

    @Override
    public boolean shouldEnable() {
        return SnowballFight.getConfiguration().getBoolean("firework-effects.enable", true)
                && has_enough_colors;
    }

    @Override
    public void enable() {
        SnowballFight plugin = SnowballFight.getInstance();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onEvent() {

    }
}