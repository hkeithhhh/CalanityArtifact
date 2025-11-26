package com.calanity.artifact.listener;

import com.calanity.artifact.CalanityArtifactPlugin;
import com.calanity.artifact.manager.ArtifactManager;
import com.calanity.artifact.model.ArtifactSet;
import com.calanity.artifact.model.EquippedArtifact;
import com.calanity.artifact.model.PlayerArtifactData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class InventoryClickListener implements Listener {

    private CalanityArtifactPlugin plugin;

    public InventoryClickListener(CalanityArtifactPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView() == null || event.getView().getTitle() == null) return;

        String title = event.getView().getTitle();

        // Prevent artifacts from being placed into vanilla armor slots — redirect to profile equip
        try {
            var slotType = event.getSlotType();
            if (slotType != null && slotType.name().equalsIgnoreCase("ARMOR")) {
                var who = event.getWhoClicked();
                if (who instanceof org.bukkit.entity.Player) {
                    org.bukkit.entity.Player p = (org.bukkit.entity.Player) who;
                    ItemStack source = null;
                    boolean fromCursor = false;
                    if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                        source = event.getCursor(); fromCursor = true;
                    } else if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                        source = event.getCurrentItem();
                    }

                    if (source != null) {
                        EquippedArtifact parsed = plugin.fromItemStack(source);
                        if (parsed != null) {
                            // cancel putting into armor, equip to profile instead
                            event.setCancelled(true);
                            var pdata = plugin.getPlayerArtifactManager().getPlayerData(p);
                            pdata.equipPiece(parsed.getPieceType(), parsed);
                            plugin.getPlayerArtifactManager().savePlayer(p.getUniqueId());
                            plugin.getPlayerArtifactManager().applyEffects(p);

                            // consume one from the source
                            if (fromCursor) {
                                ItemStack cur = event.getCursor();
                                if (cur != null) {
                                    if (cur.getAmount() > 1) {
                                        cur.setAmount(cur.getAmount() - 1);
                                        event.setCursor(cur);
                                    } else {
                                        event.setCursor(null);
                                    }
                                }
                            } else {
                                // remove one matching item from player's inventory (best-effort)
                                ItemStack toRemove = plugin.toItemStack(parsed);
                                p.getInventory().removeItem(toRemove);
                            }

                            p.sendMessage("§aEquipped " + parsed.getPieceType() + " to profile instead of armor slot.");
                            return;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        // Handle Upgrade GUI separately to allow placing inputs
        if (title.contains("Upgrade Artifact")) {
            Inventory top = event.getView().getTopInventory();
            int raw = event.getRawSlot();

            if (raw < top.getSize()) {
                // Allow placing and picking from input slots (12, 14) and result slot (13)
                if (raw == 12 || raw == 13 || raw == 14) {
                    ItemStack cursor = event.getCursor();
                    ItemStack clicked = top.getItem(raw);

                    boolean clickedIsPlaceholder = isLockedPlaceholder(clicked);

                    // Prevent picking up the placeholder itself
                    if ((cursor == null || cursor.getType() == Material.AIR) && clickedIsPlaceholder) {
                        event.setCancelled(true);
                        return;
                    }

                    // Allow placing by dragging/clicking even if clicked contains a placeholder
                    if (cursor != null && cursor.getType() != Material.AIR) {
                        // Artifact input slot
                        if (raw == 12) {
                            EquippedArtifact parsed = plugin.fromItemStack(cursor);
                            if (parsed != null) {
                                event.setCancelled(true);
                                // place one artifact into the input slot
                                top.setItem(12, plugin.toItemStack(parsed));
                                if (cursor.getAmount() > 1) {
                                    cursor.setAmount(cursor.getAmount() - 1);
                                    event.setCursor(cursor);
                                } else {
                                    event.setCursor(null);
                                }
                                return;
                            }
                        }

                        // Diamond/netherite input slot
                        if (raw == 14) {
                            if (cursor.getType() == Material.DIAMOND || cursor.getType() == Material.NETHERITE_INGOT) {
                                event.setCancelled(true);
                                // move full cursor stack into slot (player can adjust before upgrading)
                                top.setItem(14, cursor.clone());
                                event.setCursor(null);
                                return;
                            }
                        }
                    }

                    // Allow picking up real result/artifact items
                    if ((cursor == null || cursor.getType() == Material.AIR) && clicked != null && plugin.fromItemStack(clicked) != null) {
                        return;
                    }

                    // Allow shift-click to place items from inventory (let server handle general shift behavior)
                    if (event.isShiftClick()) {
                        return;
                    }

                    // Block swapping/dragging for other cases
                    event.setCancelled(true);
                    return;
                }

                // If clicked upgrade button (slot 22) handle upgrade action
                if (raw == 22) {
                    event.setCancelled(true);
                    Player player = (Player) event.getWhoClicked();
                    handleUpgradeClick(player, top);
                    return;
                }

                // Cancel other top-inventory interactions
                event.setCancelled(true);
                return;
            }

            // clicks in player's own inventory are allowed as normal
            return;
        }

        // Handle Profile GUI equip/unequip interactions
        if (title.contains("Artifact Profile")) {
            Inventory top = event.getView().getTopInventory();
            int raw = event.getRawSlot();
            int topSize = top.getSize();
            // Handle shift-click from player's inventory into profile: equip directly
            if (event.isShiftClick() && raw >= topSize) {
                var clickedInv = event.getClickedInventory();
                if (clickedInv != null) {
                    var who = event.getWhoClicked();
                    if (who instanceof Player) {
                        Player player = (Player) who;
                        ItemStack cur = event.getCurrentItem();
                        if (cur != null && cur.getType() != Material.AIR) {
                            EquippedArtifact parsed = plugin.fromItemStack(cur);
                            if (parsed != null) {
                                // find target slot for this piece type
                                int targetSlot = -1;
                                switch (parsed.getPieceType().toLowerCase()) {
                                    case "plume": targetSlot = 4; break;
                                    case "flower": targetSlot = 12; break;
                                    case "circlet": targetSlot = 13; break;
                                    case "key": targetSlot = 14; break;
                                    case "luminant": targetSlot = 22; break;
                                }
                                if (targetSlot != -1) {
                                    // Equip
                                    PlayerArtifactData pdata = plugin.getPlayerArtifactManager().getPlayerData(player);
                                    pdata.equipPiece(parsed.getPieceType(), parsed);
                                    plugin.getPlayerArtifactManager().savePlayer(player.getUniqueId());
                                    plugin.getPlayerArtifactManager().applyEffects(player);

                                    // place visual in top inventory
                                    top.setItem(targetSlot, plugin.toItemStack(parsed));

                                    // remove one from clicked slot
                                    if (cur.getAmount() > 1) {
                                        cur.setAmount(cur.getAmount() - 1);
                                        clickedInv.setItem(event.getRawSlot() - topSize, cur);
                                    } else {
                                        clickedInv.setItem(event.getRawSlot() - topSize, null);
                                    }

                                    player.sendMessage("§aEquipped " + parsed.getPieceType() + " from inventory to profile.");
                                    event.setCancelled(true);
                                    return;
                                }
                            }
                        }
                    }
                }
            }

            // profile slots mapping
            Map<Integer, String> slotToPiece = Map.of(
                4, "plume",
                12, "flower",
                13, "circlet",
                14, "key",
                22, "luminant"
            );

            if (raw < topSize && slotToPiece.containsKey(raw)) {
                Player player = (Player) event.getWhoClicked();
                String pieceType = slotToPiece.get(raw);
                PlayerArtifactData pdata = plugin.getPlayerArtifactManager().getPlayerData(player);

                ItemStack cursor = event.getCursor();
                ItemStack clicked = top.getItem(raw);

                // If player is placing an item from cursor into the slot
                if (cursor != null && cursor.getType() != Material.AIR) {
                    event.setCancelled(true);
                    EquippedArtifact parsed = plugin.fromItemStack(cursor);
                    if (parsed == null) {
                        player.sendMessage("§cThat item is not a valid artifact.");
                        return;
                    }

                    // Ensure piece matches slot
                    if (!parsed.getPieceType().equalsIgnoreCase(pieceType)) {
                        player.sendMessage("§cThis artifact doesn't belong in that slot.");
                        return;
                    }

                    // Equip
                    pdata.equipPiece(pieceType, parsed);
                    plugin.getPlayerArtifactManager().savePlayer(player.getUniqueId());
                    // Apply effects after equipping
                    plugin.getPlayerArtifactManager().applyEffects(player);

                    // Decrease cursor stack by 1 (or clear)
                    if (cursor.getAmount() > 1) {
                        cursor.setAmount(cursor.getAmount() - 1);
                        event.setCursor(cursor);
                    } else {
                        event.setCursor(null);
                    }

                    // Update GUI slot to show equipped artifact
                    top.setItem(raw, plugin.toItemStack(parsed));
                    player.sendMessage("§aEquipped " + parsed.getPieceType() + " from set " + parsed.getSetId());
                    return;
                }

                // If player is picking up from the slot (unequip) or shift-clicking
                if (((cursor == null || cursor.getType() == Material.AIR) && clicked != null && clicked.getType() != Material.GRAY_STAINED_GLASS_PANE) || 
                    (event.isShiftClick() && clicked != null && clicked.getType() != Material.GRAY_STAINED_GLASS_PANE)) {
                    event.setCancelled(true);
                    EquippedArtifact equipped = pdata.getEquippedArtifact(pieceType);
                    if (equipped == null) {
                        player.sendMessage("§cNo equipped artifact in that slot.");
                        return;
                    }

                    // Remove from player data
                    pdata.unequipPiece(pieceType);
                    plugin.getPlayerArtifactManager().savePlayer(player.getUniqueId());
                    // Recompute and apply effects after unequipping
                    plugin.getPlayerArtifactManager().applyEffects(player);

                    // Give item back to player (try inventory first)
                    ItemStack give = plugin.toItemStack(equipped);
                    Map<Integer, org.bukkit.inventory.ItemStack> leftover = player.getInventory().addItem(give);
                    if (!leftover.isEmpty()) {
                        player.sendMessage("§eInventory full, dropping artifact on ground.");
                        player.getWorld().dropItemNaturally(player.getLocation(), give);
                    }

                    // Clear GUI slot
                    top.setItem(raw, createProfilePlaceholder(slotToPiece.get(raw)));
                    player.sendMessage("§eUnequipped " + equipped.getPieceType());
                    return;
                }

                // Block other interactions on artifact slots
                event.setCancelled(true);
                return;
            }
            // Cancel other profile top-inventory interactions
            if (raw < topSize) {
                event.setCancelled(true);
            }
            return;
        }

        // Default: prevent interactions with other artifact-related GUIs to avoid item theft
        if (title.contains("Artifact")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView() == null || event.getView().getTitle() == null) return;
        String title = event.getView().getTitle();
        if (!title.contains("Upgrade Artifact")) return;

        // Only allow dragging into the artifact input (12) or diamond input (14)
        boolean allowed = true;
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot == 12 || rawSlot == 14) continue;
            // if any raw slot is inside top inventory and not allowed, cancel
            if (rawSlot < event.getView().getTopInventory().getSize()) {
                allowed = false;
                break;
            }
        }

        if (!allowed) event.setCancelled(true);
    }

    

    private ItemStack createProfilePlaceholder(String pieceType) {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = pieceType == null ? "Empty" : capitalize(pieceType);
            meta.setDisplayName("§7" + name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createLockedPlaceholder(String text, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7" + text);
            item.setItemMeta(meta);
        }
        return item;
    }

    private boolean isLockedPlaceholder(ItemStack item) {
        if (item == null) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        if (!meta.hasLore()) return false;
        for (String l : meta.getLore()) {
            String clean = l.replaceAll("§.", "");
            if (clean.trim().equalsIgnoreCase("Place item here")) return true;
        }
        // also detect common placeholder display names
        if (meta.hasDisplayName()) {
            String dn = meta.getDisplayName().replaceAll("§.", "").toLowerCase();
            if (dn.contains("place artifact") || dn.contains("place diamonds") || dn.contains("result")) return true;
        }
        return false;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private void handleUpgradeClick(Player player, Inventory top) {
        try {
            ItemStack artifactItem = top.getItem(12);
            ItemStack diamondItem = top.getItem(14);

            if (artifactItem == null || !artifactItem.hasItemMeta()) {
                player.sendMessage("§cPlace an artifact in the left slot first.");
                return;
            }

            // Identify artifact from the item using PDC or fallback parsing
            EquippedArtifact itemEq = plugin.fromItemStack(artifactItem);
            if (itemEq == null) {
                player.sendMessage("§cThis item is not a valid artifact.");
                return;
            }

            if (!itemEq.canUpgrade()) {
                player.sendMessage("§cThis artifact is already at max level.");
                return;
            }

            // Determine cost type (diamond vs netherite)
            if (itemEq.isNetheriteCost()) {
                int cost = itemEq.getUpgradeNetheriteCost();
                if (diamondItem == null || diamondItem.getType() != Material.NETHERITE_INGOT || diamondItem.getAmount() < cost) {
                    player.sendMessage("§cYou need " + cost + " netherite ingot(s) in the diamond slot to upgrade.");
                    return;
                }

                // Consume netherite
                int remaining = diamondItem.getAmount() - cost;
                if (remaining <= 0) {
                    top.setItem(14, null);
                } else {
                    diamondItem.setAmount(remaining);
                    top.setItem(14, diamondItem);
                }
            } else {
                int cost = itemEq.getUpgradeDiamondCost();
                if (diamondItem == null || diamondItem.getType() != Material.DIAMOND || diamondItem.getAmount() < cost) {
                    player.sendMessage("§cYou need " + cost + " diamonds in the diamond slot to upgrade.");
                    return;
                }

                // Consume diamonds from diamondInput slot
                int remaining = diamondItem.getAmount() - cost;
                if (remaining <= 0) {
                    top.setItem(14, null);
                } else {
                    diamondItem.setAmount(remaining);
                    top.setItem(14, diamondItem);
                }
            }

            // Compute pre-upgrade snapshot of effects
            java.util.Map<String, Double> before = new java.util.HashMap<>();
            if (itemEq.getPotionEffects() != null) before.putAll(itemEq.getPotionEffects());

            // Perform upgrade on the item artifact
            itemEq.upgrade();

            // If the player currently has this piece equipped, update their equipped data as well
            try {
                PlayerArtifactData pdataUp = plugin.getPlayerArtifactManager().getPlayerData(player);
                var equippedNow = pdataUp.getEquippedArtifact(itemEq.getPieceType());
                if (equippedNow != null && equippedNow.getSetId().equals(itemEq.getSetId())) {
                    pdataUp.equipPiece(itemEq.getPieceType(), itemEq);
                    plugin.getPlayerArtifactManager().savePlayer(player.getUniqueId());
                    plugin.getPlayerArtifactManager().applyEffects(player);
                }
            } catch (Exception ignored) {}

            // Create result item (PDC-tagged) and place in result slot
            ItemStack result = plugin.toItemStack(itemEq);
            top.setItem(13, result);

            // Clear artifact input slot after successful upgrade
            top.setItem(12, createLockedPlaceholder("Place Artifact Here", Material.GRAY_STAINED_GLASS_PANE));

            // Compute diff of potion effects to inform player
            java.util.Map<String, Double> after = itemEq.getPotionEffects() != null ? itemEq.getPotionEffects() : java.util.Collections.emptyMap();
            java.util.List<String> added = new java.util.ArrayList<>();
            java.util.List<String> increased = new java.util.ArrayList<>();
            for (var e : after.entrySet()) {
                String k = e.getKey();
                double v = e.getValue();
                if (!before.containsKey(k)) {
                    added.add(k + " (" + String.format("%.1f", v) + ")");
                } else {
                    double oldv = before.get(k);
                    if (v > oldv) increased.add(k + " " + String.format("%.1f", oldv) + "→" + String.format("%.1f", v));
                }
            }

            if (!added.isEmpty()) {
                player.sendMessage("§aUpgrade added effects: §f" + String.join(", ", added));
            }
            if (!increased.isEmpty()) {
                player.sendMessage("§eUpgrade increased effects: §f" + String.join(", ", increased));
            }

            player.sendMessage("§aUpgrade successful! New level: §e" + itemEq.getLevel());
        } catch (Exception ex) {
            plugin.getLogger().warning("Error handling upgrade: " + ex.getMessage());
            player.sendMessage("§cAn error occurred while upgrading.");
        }
    }

    private ItemStack buildResultItem(EquippedArtifact equipped) {
        ArtifactSet set = plugin.getArtifactManager().getArtifactSet(equipped.getSetId());
        org.bukkit.Material mat = org.bukkit.Material.DIAMOND;
        String display = equipped.getPieceType();
        if (set != null) {
            var piece = set.getPiece(equipped.getPieceType());
            if (piece != null) {
                var m = org.bukkit.Material.matchMaterial(piece.getMaterial());
                if (m != null) mat = m;
                display = piece.getDisplayName();
            }
        }

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§l§6" + display + " §8[Lvl " + equipped.getLevel() + "/20] ");
            List<String> lore = new java.util.ArrayList<>();
            lore.add("§7Result of upgrade:");
            if (equipped.getStat("atk") > 0) lore.add("§c+ATK: §f" + String.format("%.1f", equipped.getStat("atk")) + "%");
            if (equipped.getStat("defense") > 0) lore.add("§9+DEF: §f" + String.format("%.1f", equipped.getStat("defense")) + "%");
            if (equipped.getStat("heal_regen") > 0) lore.add("§a+HEAL: §f" + String.format("%.1f", equipped.getStat("heal_regen")) + "%");
            if (equipped.getStat("hunger") > 0) lore.add("§e+HUNGER: §f" + String.format("%.1f", equipped.getStat("hunger")) + "%");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }
}
