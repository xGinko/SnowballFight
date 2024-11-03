package me.xginko.snowballfight.commands.snowballs.subcommands;

import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.commands.SubCommand;
import me.xginko.snowballfight.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class ReloadSubCmd extends SubCommand {

    @Override
    public String getLabel() {
        return "reload";
    }

    @Override
    public TextComponent getDescription() {
        return Component.text("Reload the plugin configuration.").color(NamedTextColor.GRAY);
    }

    @Override
    public TextComponent getSyntax() {
        return Component.text("/snowballs reload").color(NamedTextColor.WHITE);
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission("snowballfight.cmd.reload")) return;

        Util.sendMessage(sender, Component.text("Reloading SnowballFight...").color(NamedTextColor.WHITE));
        SnowballFight.scheduling().asyncScheduler().run(() -> {
            SnowballFight.getInstance().reloadConfiguration();
            Util.sendMessage(sender, Component.text("Reload complete.").color(NamedTextColor.GREEN));
        });
    }
}