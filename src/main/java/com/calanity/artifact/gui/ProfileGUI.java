package com.calanity.artifact.gui;

import com.calanity.artifact.CalanityArtifactPlugin;
import com.calanity.artifact.model.ArtifactSet;
import com.calanity.artifact.model.ArtifactPiece;
import com.calanity.artifact.model.EquippedArtifact;
import com.calanity.artifact.model.PlayerArtifactData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ProfileGUI {

    private CalanityArtifactPlugin plugin;
    private Player player;
    private Inventory inventory;

    public ProfileGUI(CalanityArtifactPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        createInventory();
    }

    private void createInventory() {
        // 3 rows = 27 slots
        inventory = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "&c&lArtifact Profile"));
        fillEquippedPieces();
    }

    private void fillEquippedPieces() {
        PlayerArtifactData playerData = plugin.getPlayerArtifactManager().getPlayerData(player);
        
        // Centered cross pattern (3 rows x 9 cols = 27 slots):
        // Row 0: columns 0..8 -> plume at column 4 (slot 4)
        // Row 1: flower at column 3 (slot 12), circlet at column 4 (slot 13), key at column 5 (slot 14)
        // Row 2: luminant at column 4 (slot 22)
        // Slot indices:
        // Plume: slot 4  (row 0, col 4)
        // Flower: slot 12 (row 1, col 3)
        // Circlet: slot 13 (row 1, col 4) - CENTER
        // Key: slot 14 (row 1, col 5)
        // Luminant: slot 22 (row 2, col 4)
        
        ItemStack plume = createEquippedItem("plume", playerData.getEquippedArtifact("plume"));
        ItemStack flower = createEquippedItem("flower", playerData.getEquippedArtifact("flower"));
        ItemStack key = createEquippedItem("key", playerData.getEquippedArtifact("key"));
        ItemStack circlet = createEquippedItem("circlet", playerData.getEquippedArtifact("circlet"));
        ItemStack luminant = createEquippedItem("luminant", playerData.getEquippedArtifact("luminant"));
        
        inventory.setItem(4, plume);
        inventory.setItem(12, flower);
        inventory.setItem(13, circlet);
        inventory.setItem(14, key);
        inventory.setItem(22, luminant);
    }

    private ItemStack createEquippedItem(String pieceType, EquippedArtifact equipped) {
        if (equipped == null) {
            // Empty slot
            ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&7" + capitalize(pieceType)));
                item.setItemMeta(meta);
            }
            return item;
        }
        
        ArtifactSet set = plugin.getArtifactManager().getArtifactSet(equipped.getSetId());
        if (set == null) return new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        
        ArtifactPiece piece = set.getPiece(equipped.getPieceType());
        if (piece == null) return new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        
        Material material = Material.matchMaterial(piece.getMaterial());
        if (material == null) material = Material.DIAMOND;
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', 
                "&l" + piece.getDisplayName() + " &8[Lvl " + equipped.getLevel() + "/20&8]"));
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7" + set.getDisplayName() + " - " + capitalize(pieceType)));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&f$ " + piece.getPrice()));
            lore.add("");
            
            // Show stats
            if (equipped.getStat("atk") > 0) {
                lore.add(ChatColor.translateAlternateColorCodes('&', "&c+ATK: &f" + String.format("%.1f", equipped.getStat("atk")) + "%"));
            }
            if (equipped.getStat("defense") > 0) {
                lore.add(ChatColor.translateAlternateColorCodes('&', "&9+DEF: &f" + String.format("%.1f", equipped.getStat("defense")) + "%"));
            }
            if (equipped.getStat("heal_regen") > 0) {
                lore.add(ChatColor.translateAlternateColorCodes('&', "&a+HEAL: &f" + String.format("%.1f", equipped.getStat("heal_regen")) + "%"));
            }
            if (equipped.getStat("hunger") > 0) {
                lore.add(ChatColor.translateAlternateColorCodes('&', "&e+HUNGER: &f" + String.format("%.1f", equipped.getStat("hunger")) + "%"));
            }

            // Show potion effects
            if (equipped.getPotionEffects() != null && !equipped.getPotionEffects().isEmpty()) {
                lore.add("");
                lore.add(ChatColor.translateAlternateColorCodes('&', "&6Active Effects:"));
                for (var e : equipped.getPotionEffects().entrySet()) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&b" + e.getKey() + ": &f" + String.format("%.1f", e.getValue())));
                }
            }
            
            for (String line : piece.getLore()) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public void open() {
        player.openInventory(inventory);
    }

    public static class ChatColor {
        public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
            char[] b = textToTranslate.toCharArray();
            for (int i = 0; i < b.length - 1; i++) {
                if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1) {
                    b[i] = '§';
                    b[i+1] = Character.toLowerCase(b[i+1]);
                }
            }
            return new String(b);
        }
    }
}
