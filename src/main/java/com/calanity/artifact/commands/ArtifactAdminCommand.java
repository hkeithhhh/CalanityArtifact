package com.calanity.artifact.commands;

import com.calanity.artifact.CalanityArtifactPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArtifactAdminCommand implements CommandExecutor {

    private CalanityArtifactPlugin plugin;

    public ArtifactAdminCommand(CalanityArtifactPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("calanityartifact.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§c/artifactadmin reload - Reload all configs");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfigs();
            sender.sendMessage("§aConfigs reloaded!");
            return true;
        }

        sender.sendMessage("§cUnknown subcommand!");
        return true;
    }
}
