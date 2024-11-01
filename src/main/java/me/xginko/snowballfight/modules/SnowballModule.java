package me.xginko.snowballfight.modules;


import java.util.HashSet;

public interface SnowballModule {

    boolean shouldEnable();
    void enable();
    void disable();

    HashSet<SnowballModule> modules = new HashSet<>();

    static void disableAll() {
        modules.forEach(SnowballModule::disable);
        modules.clear();
    }

    static void reloadModules() {
        disableAll();

        modules.add(new InfiniteSnowballs());
        modules.add(new DamageOnHit());
        modules.add(new ExplodeOnHit());
        modules.add(new FireworkOnHit());
        modules.add(new SnowOnHit());
        modules.add(new RemoveArmorOnHit());
        modules.add(new KnockbackOnHit());
        modules.add(new LevitateOnHit());
        modules.add(new LightningOnHit());
        modules.add(new SlownessOnHit());
        modules.add(new ThrowCoolDown());
        modules.add(new TrailsWhenThrown());

        modules.forEach(module -> {
            if (module.shouldEnable()) module.enable();
        });
    }
}