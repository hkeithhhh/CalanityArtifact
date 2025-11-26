package com.calanity.artifact.listener;

import com.calanity.artifact.CalanityArtifactPlugin;
import com.calanity.artifact.model.EquippedArtifact;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.Action;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AutoEquipListener implements Listener {

    private final CalanityArtifactPlugin plugin;
    private final Map<UUID, BukkitTask> pending = new ConcurrentHashMap<>();

    public AutoEquipListener(CalanityArtifactPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getPlayer() == null) return;

        ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();
        if (hand == null) return;

        EquippedArtifact eq = plugin.fromItemStack(hand);
        if (eq == null) return; // not an artifact

        UUID uid = event.getPlayer().getUniqueId();
        // cancel existing pending task if any
        var prev = pending.remove(uid);
        if (prev != null) prev.cancel();

        // schedule a delayed check (1.5s = 30 ticks)
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                ItemStack now = event.getPlayer().getInventory().getItemInMainHand();
                if (now == null) return;
                EquippedArtifact nowEq = plugin.fromItemStack(now);
                if (nowEq == null) return;
                // match by setId and pieceType (and level) to ensure same item
                if (!nowEq.getSetId().equals(eq.getSetId())) return;
                if (!nowEq.getPieceType().equals(eq.getPieceType())) return;
                // Auto-equip: consume one from hand and save
                if (now.getAmount() > 1) {
                    now.setAmount(now.getAmount() - 1);
                    event.getPlayer().getInventory().setItemInMainHand(now);
                } else {
                    event.getPlayer().getInventory().setItemInMainHand(null);
                }

                plugin.getPlayerArtifactManager().getPlayerData(event.getPlayer()).equipPiece(nowEq.getPieceType(), nowEq);
                plugin.getPlayerArtifactManager().savePlayer(event.getPlayer().getUniqueId());
                plugin.getPlayerArtifactManager().applyEffects(event.getPlayer());
                event.getPlayer().sendMessage("§aAuto-equipped " + nowEq.getPieceType() + " from set " + nowEq.getSetId());
            } catch (Exception ignored) {
            }
        }, 30L);

        pending.put(uid, task);
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        var t = pending.remove(event.getPlayer().getUniqueId());
        if (t != null) t.cancel();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        var t = pending.remove(event.getPlayer().getUniqueId());
        if (t != null) t.cancel();
    }
}
