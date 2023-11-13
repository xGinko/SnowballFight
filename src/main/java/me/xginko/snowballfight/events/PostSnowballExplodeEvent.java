package me.xginko.snowballfight.events;


import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PostSnowballExplodeEvent extends org.bukkit.event.Event {

    private static final @NotNull HandlerList handlers = new HandlerList();


    public PostSnowballExplodeEvent(

    ) {

    }



    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
