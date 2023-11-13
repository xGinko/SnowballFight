package me.xginko.snowballfight.commands.snowballs.subcommands;

import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.commands.SubCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

public class VersionSubCmd extends SubCommand {

    @Override
    public String getLabel() {
        return "version";
    }

    @Override
    public TextComponent getDescription() {
        return Component.text("Show the plugin version.").color(NamedTextColor.GRAY);
    }

    @Override
    public TextComponent getSyntax() {
        return Component.text("/snowballs version").color(NamedTextColor.WHITE);
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission("snowballfight.cmd.version")) return;
        final PluginDescriptionFile pluginMeta = SnowballFight.getInstance().getDescription();
        sender.sendMessage(
                Component.newline()
                .append(
                        Component.text(pluginMeta.getName()+" "+pluginMeta.getVersion())
                        .color(NamedTextColor.GOLD)
                        .clickEvent(ClickEvent.openUrl(pluginMeta.getWebsite()))
                )
                .append(Component.text(" by ").color(NamedTextColor.GRAY))
                .append(
                        Component.text(pluginMeta.getAuthors().get(0))
                        .color(NamedTextColor.DARK_AQUA)
                        .clickEvent(ClickEvent.openUrl("https://github.com/xGinko"))
                )
                .append(Component.newline())
        );
    }
}