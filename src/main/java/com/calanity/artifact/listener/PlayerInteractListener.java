package com.calanity.artifact.listener;

import com.calanity.artifact.CalanityArtifactPlugin;
import com.calanity.artifact.model.Artifact;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {

    private CalanityArtifactPlugin plugin;

    public PlayerInteractListener(CalanityArtifactPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // This listener can be used for right-click artifact interactions
        // Currently reserved for future features
    }
}
