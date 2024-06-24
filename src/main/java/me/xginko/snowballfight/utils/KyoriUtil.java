package me.xginko.snowballfight.utils;

import me.xginko.snowballfight.SnowballFight;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public final class KyoriUtil {

    public static void sendMessage(CommandSender sender, Component message) {
        SnowballFight.getAudiences().sender(sender).sendMessage(message);
    }
}
