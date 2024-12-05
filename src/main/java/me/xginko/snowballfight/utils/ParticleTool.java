package me.xginko.snowballfight.utils;

import com.cryptomorin.xseries.particles.XParticle;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

public class ParticleTool {

    private static final boolean HAS_PARTICLE_BUILDER, HAS_DUST_OPTIONS;

    static {
        HAS_PARTICLE_BUILDER = Util.hasClass("com.destroystokyo.paper.ParticleBuilder");
        HAS_DUST_OPTIONS = Util.hasClass("org.bukkit.Particle$DustOptions");
    }

    public static void spawnTrailParticle(Location location, Color color, int amount, float size) {
        if (HAS_PARTICLE_BUILDER) {
            ParticleBuilder builder = new ParticleBuilder(XParticle.DUST.get())
                    .location(location)
                    .count(amount);
            (HAS_DUST_OPTIONS ? builder.color(color, size) : builder.color(color))
                    .spawn();
            return;
        }

        // spigot 1.13+
        if (HAS_DUST_OPTIONS) {
            location.getWorld().spawnParticle(
                    XParticle.DUST.get(),
                    location,
                    amount,
                    0, 0, 0,
                    1,
                    new Particle.DustOptions(color, size)
            );
            return;
        }

        // spigot 1.12
        location.getWorld().spawnParticle(
                XParticle.DUST.get(),
                location,
                amount,
                convertColorValue(color.getRed()), convertColorValue(color.getGreen()), convertColorValue(color.getBlue()),
                1,
                null
        );
    }

    private static double convertColorValue(double value) {
        if (value <= 0.0D) {
            value = -1.0D;
        }
        return value / 255.0D;
    }
}
