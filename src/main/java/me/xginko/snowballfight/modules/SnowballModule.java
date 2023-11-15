package me.xginko.snowballfight.modules;


import me.xginko.snowballfight.modules.effects.FireworkExplosions;
import me.xginko.snowballfight.modules.effects.LightningEffects;
import me.xginko.snowballfight.modules.effects.LevitateOnHit;
import me.xginko.snowballfight.modules.effects.TrailsWhenThrown;
import me.xginko.snowballfight.modules.triggers.ExplodeOnHit;

import java.util.HashSet;

public interface SnowballModule {

    boolean shouldEnable();
    void enable();
    void disable();

    HashSet<SnowballModule> modules = new HashSet<>();

    static void reloadModules() {
        modules.forEach(SnowballModule::disable);
        modules.clear();

        modules.add(new ExplodeOnHit());
        modules.add(new FireworkExplosions());
        modules.add(new LightningEffects());
        modules.add(new TrailsWhenThrown());
        modules.add(new LevitateOnHit());

        modules.forEach(module -> {
            if (module.shouldEnable()) module.enable();
        });
    }
}
