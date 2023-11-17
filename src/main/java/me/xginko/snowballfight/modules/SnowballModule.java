package me.xginko.snowballfight.modules;


import java.util.HashSet;

public interface SnowballModule {

    boolean shouldEnable();
    void enable();
    void disable();

    HashSet<SnowballModule> modules = new HashSet<>();

    static void reloadModules() {
        modules.forEach(SnowballModule::disable);
        modules.clear();

        modules.add(new DamageOnHit());
        modules.add(new ExplodeOnHit());
        modules.add(new FireworkOnHit());
        modules.add(new LightningOnHit());
        modules.add(new TrailsWhenThrown());
        modules.add(new LevitateOnHit());

        modules.forEach(module -> {
            if (module.shouldEnable()) module.enable();
        });
    }
}
