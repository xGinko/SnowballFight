package me.xginko.snowballfight.commands.snowballs;

import com.google.common.collect.ImmutableList;
import me.xginko.snowballfight.commands.SubCommand;
import me.xginko.snowballfight.commands.snowballs.subcommands.DisableSubCmd;
import me.xginko.snowballfight.commands.snowballs.subcommands.ReloadSubCmd;
import me.xginko.snowballfight.commands.snowballs.subcommands.VersionSubCmd;
import me.xginko.snowballfight.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SnowballsCommand implements TabCompleter, CommandExecutor {

    private final List<SubCommand> subCommands;
    private final List<String> tabCompleter;

    public SnowballsCommand() {
        this.subCommands = ImmutableList.of(new ReloadSubCmd(), new VersionSubCmd(), new DisableSubCmd());
        this.tabCompleter = subCommands.stream().map(SubCommand::getLabel)
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        return args.length == 1 ? tabCompleter : Collections.emptyList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sendCommandOverview(sender);
            return true;
        }

        for (final SubCommand subCommand : subCommands) {
            if (args[0].equalsIgnoreCase(subCommand.getLabel())) {
                subCommand.perform(sender, args);
                return true;
            }
        }

        sendCommandOverview(sender);
        return true;
    }

    private void sendCommandOverview(CommandSender sender) {
        if (!sender.hasPermission("snowballfight.cmd.*")) return;

        Util.sendMessage(sender, Component.text("-----------------------------------------------------").color(Util.SNOWY_DARK_BLUE));
        Util.sendMessage(sender, Component.text(" SnowballFight Commands").color(Util.SNOWY_WHITE));
        Util.sendMessage(sender, Component.text("-----------------------------------------------------").color(Util.SNOWY_DARK_BLUE));
        subCommands.forEach(subCommand -> Util.sendMessage(sender,
                subCommand.getSyntax().append(Component.text(" - ").color(NamedTextColor.DARK_GRAY)).append(subCommand.getDescription())));
        Util.sendMessage(sender, Component.text("-----------------------------------------------------").color(Util.SNOWY_DARK_BLUE));
    }
}