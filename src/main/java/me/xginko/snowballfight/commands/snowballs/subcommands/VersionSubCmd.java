package me.xginko.snowballfight.commands.snowballs.subcommands;

import me.xginko.snowballfight.SnowballFight;
import me.xginko.snowballfight.commands.SubCommand;
import me.xginko.snowballfight.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

public class VersionSubCmd extends SubCommand {

    @Override
    public String getLabel() {
        return "version";
    }

    @Override
    public TextComponent getDescription() {
        return Component.text("Show the plugin version.").color(Util.SNOWY_BLUE);
    }

    @Override
    public TextComponent getSyntax() {
        return Component.text("/snowballs version").color(Util.SNOWY_WHITE);
    }

    @Override
    @SuppressWarnings({"deprecation", "UnstableApiUsage"})
    public void perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission("snowballfight.cmd.version")) return;

        String name, version, website, author;

        try {
            final io.papermc.paper.plugin.configuration.PluginMeta pluginMeta = SnowballFight.getInstance().getPluginMeta();
            name = pluginMeta.getName();
            version = pluginMeta.getVersion();
            website = pluginMeta.getWebsite();
            author = pluginMeta.getAuthors().get(0);
        } catch (Throwable versionIncompatible) {
            final PluginDescriptionFile pluginYML = SnowballFight.getInstance().getDescription();
            name = pluginYML.getName();
            version = pluginYML.getVersion();
            website = pluginYML.getWebsite();
            author = pluginYML.getAuthors().get(0);
        }

        Util.sendMessage(sender, Component.newline()
                .append(
                        Component.text(name + " " + version)
                                .color(Util.SNOWY_WHITE)
                                .clickEvent(ClickEvent.openUrl(website))
                )
                .append(Component.text(" by ").color(Util.SNOWY_DARK_BLUE))
                .append(
                        Component.text(author)
                                .color(Util.SNOWY_BLUE)
                                .clickEvent(ClickEvent.openUrl("https://github.com/xGinko"))
                )
                .append(Component.newline())
        );
    }
}