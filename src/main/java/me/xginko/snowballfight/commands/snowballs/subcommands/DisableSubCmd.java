package me.xginko.snowballfight.commands.snowballs.subcommands;

import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.commands.SubCommand;
import me.xginko.snowballfight.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.CommandSender;

public class DisableSubCmd extends SubCommand {

    @Override
    public String getLabel() {
        return "disable";
    }

    @Override
    public TextComponent getDescription() {
        return Component.text("Disable all plugin tasks and listeners.").color(Util.SNOWY_BLUE);
    }

    @Override
    public TextComponent getSyntax() {
        return Component.text("/snowballs disable").color(Util.SNOWY_WHITE);
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission("snowballfight.cmd.disable")) return;

        Util.sendMessage(sender, Component.text("Disabling SnowballFight...").color(Util.SNOWY_RED));

        SnowballFight.getInstance().disableRunningTasks();

        Util.sendMessage(sender, Component.text("Disabled all plugin listeners and tasks.").color(Util.SNOWY_BLUE));
        Util.sendMessage(sender, Component.text("You can enable the plugin again using the reload command.").color(Util.SNOWY_DARK_BLUE));
    }
}