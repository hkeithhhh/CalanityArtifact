package com.calanity.artifact.commands;

import com.calanity.artifact.CalanityArtifactPlugin;
import com.calanity.artifact.gui.ProfileGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProfileCommand implements CommandExecutor {

    private CalanityArtifactPlugin plugin;

    public ProfileCommand(CalanityArtifactPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        ProfileGUI gui = new ProfileGUI(plugin, player);
        gui.open();
        player.sendMessage("§aOpening your artifact profile...");

        return true;
    }
}
