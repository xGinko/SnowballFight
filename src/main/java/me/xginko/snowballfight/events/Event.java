package me.xginko.snowballfight.events;


import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class Event extends org.bukkit.event.Event {

    private static final @NotNull HandlerList handlers = new HandlerList();


    public Event(

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
