package me.xginko.snowballfight.adventure;

import me.xginko.snowballfight.SnowballFight;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public class KyoriUtil {
    public static void sendMessage(CommandSender sender, Component message) {
        SnowballFight.getAudiences().sender(sender).sendMessage(message);
    }
}
