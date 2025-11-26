package com.calanity.artifact.commands;

import com.calanity.artifact.CalanityArtifactPlugin;
import com.calanity.artifact.model.EquippedArtifact;
import com.calanity.artifact.model.ArtifactSet;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveArtifactCommand implements CommandExecutor {

    private CalanityArtifactPlugin plugin;

    public GiveArtifactCommand(CalanityArtifactPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("calanityartifact.give")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /giveartifact <player> <setId> <piece>");
            sender.sendMessage("§cExample: /giveartifact Steve CRIMSON circlet");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        String setId = args[1].toUpperCase();
        String piece = args[2].toLowerCase();

        ArtifactSet set = plugin.getArtifactManager().getArtifactSet(setId);
        if (set == null) {
            sender.sendMessage("§cArtifact set '" + setId + "' not found!");
            return true;
        }

        if (set.getPiece(piece) == null) {
            sender.sendMessage("§cPiece '" + piece + "' not found in set " + setId + "!");
            return true;
        }

        // Create an EquippedArtifact and give the item to player's inventory (NO auto-equip)
        EquippedArtifact eq = new EquippedArtifact(setId, piece);

        // Give the serialized artifact ItemStack so the player can place it in GUIs
        try {
            var item = plugin.toItemStack(eq);
            var leftovers = target.getInventory().addItem(item);
            if (!leftovers.isEmpty()) {
                // drop if inventory full
                target.getWorld().dropItemNaturally(target.getLocation(), item);
            }
        } catch (Exception ignored) {}

        sender.sendMessage("§aGave " + piece + " of set " + setId + " to " + target.getName());
        target.sendMessage("§aYou received " + piece + " of set " + setId + " in your inventory!");
        return true;
    }
}
