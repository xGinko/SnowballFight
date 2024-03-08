package me.xginko.snowballfight.commands.snowballs.subcommands;

import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.commands.SubCommand;
import me.xginko.snowballfight.modules.SnowballModule;
import me.xginko.snowballfight.adventure.KyoriUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class DisableSubCmd extends SubCommand {

    @Override
    public String getLabel() {
        return "disable";
    }

    @Override
    public TextComponent getDescription() {
        return Component.text("Disable all plugin tasks and listeners.").color(NamedTextColor.GRAY);
    }

    @Override
    public TextComponent getSyntax() {
        return Component.text("/snowballs disable").color(NamedTextColor.WHITE);
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission("snowballfight.cmd.disable")) return;

        KyoriUtil.sendMessage(sender, Component.text("Disabling SnowballFight...").color(NamedTextColor.RED));

        SnowballModule.modules.forEach(SnowballModule::disable);
        SnowballModule.modules.clear();
        SnowballFight.getCache().cacheMap().clear();

        KyoriUtil.sendMessage(sender, Component.text("Disabled all plugin listeners and tasks.").color(NamedTextColor.GREEN));
        KyoriUtil.sendMessage(sender, Component.text("You can enable the plugin again using the reload command.").color(NamedTextColor.GRAY));
    }
}