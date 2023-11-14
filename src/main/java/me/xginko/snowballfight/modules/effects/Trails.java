package me.xginko.snowballfight.modules.effects;

import com.destroystokyo.paper.ParticleBuilder;
import me.xginko.snowballfight.SnowballConfig;
import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.events.PostSnowballExplodeEvent;
import me.xginko.snowballfight.modules.SnowballModule;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Trails implements SnowballModule, Listener {

    private final List<ParticleBuilder> particleBuilders = new ArrayList<>();
    private boolean has_enough_colors = true;

    public Trails() {
        shouldEnable();
        SnowballConfig config = SnowballFight.getConfiguration();
        List<String> configuredColors = config.getList("trails.colors", List.of(
                "<color:#B3E3F4>",   // Snowy Dark Sky
                "<color:#EEB64B>",   // Yellow
                "<color:#B5E5E7>",   // Early Winter Snow White slightly Blue
                "<color:#FC9460>",   // Orange
                "<color:#71C3DB>",   // Wet Snow Blue White
                "<color:#E54264>",   // Red
                "<color:#9BDBFF>",   // Mid Winter White slightly more Blue
                "<color:#A92F5F>",   // Dark Red
                "<color:#E8EBF0>",   // Frost on thin twigs White
                "<color:#442261>",   // Purple
                "<color:#F198AF>",   // Evening cloud Pinkred
                "<color:#283D5E>",   // Dark Blue
                "<color:#FEDBB3>",   // Sun slightly red on snow reflection
                "<color:#327F51>",   // Green
                "<color:#59B1BD>",   // Mid day snow shadow blue
                "<color:#64A47F>",   // Light Green
                "<color:#60798D>",   // Evening slightly red sun snow shadow
                "<color:#C3C48A>",   // Light Yellow-Green
                "<color:#407794>"    // Evening slightly red sun snow shadow but more blue
        ), "You need to configure at least 2 colors. Uses MiniMessage formatting.");
        if (configuredColors.size() < 2) {
            SnowballFight.getLog().severe("You need to configure at least 2 colors. Disabling firework effects.");
            has_enough_colors = false;
        }
        List<Color> parsedColors = new ArrayList<>();
        configuredColors.forEach(hexString -> {
            final TextColor textColor = MiniMessage.miniMessage().deserialize(hexString).color();
            if (textColor == null) {
                SnowballFight.getLog().warning("Hex color string '"+hexString+"' is not formatted correctly. Use it like this: <color:#E54264>");
            } else {
                parsedColors.add(Color.fromRGB(textColor.red(), textColor.green(), textColor.blue()));
            }
        });
        final int particles_per_tick = config.getInt("trails.particle-count-per-tick", 10);
        parsedColors.forEach(primary_color -> {
            Color secondary_color = primary_color;
            int tries = 0;
            while (secondary_color.equals(primary_color)) { // Avoid rolling the same color
                if (tries > 100) break; // Avoid infinite loop on bad config
                secondary_color = parsedColors.get(new Random().nextInt(parsedColors.size() + 1) - 1);
                tries++;
            }
            this.particleBuilders.add(new ParticleBuilder(Particle.FIREWORKS_SPARK).color(primary_color).count(particles_per_tick));
            this.particleBuilders.add(new ParticleBuilder(Particle.FIREWORKS_SPARK).color(secondary_color).count(particles_per_tick));
        });
    }

    @Override
    public boolean shouldEnable() {
        return SnowballFight.getConfiguration().getBoolean("trails.enable", true) && has_enough_colors;
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
    private void onPostSnowballExplode(PostSnowballExplodeEvent event) {

    }
}