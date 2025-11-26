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

public class UpgradeGUI {

    private CalanityArtifactPlugin plugin;
    private Player player;
    private Inventory inventory;

    public UpgradeGUI(CalanityArtifactPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        createInventory();
    }

    private static final int SLOT_ARTIFACT_INPUT = 12; // row1 col3
    private static final int SLOT_RESULT = 13; // center
    private static final int SLOT_DIAMOND_INPUT = 14; // row1 col5
    private static final int SLOT_UPGRADE_BUTTON = 22; // row2 col4

    private void createInventory() {
        // 3 rows = 27 slots
        inventory = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "&c&lUpgrade Artifact"));
        fillUpgradeSlots();
    }

    private void fillUpgradeSlots() {
        // Fill with placeholders and three functional slots: artifact input, diamond input, result, and upgrade button
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, createPlaceholderPane());
        }

        // Artifact input uses a Diamond Block icon so it's obvious where to put artifacts
        inventory.setItem(SLOT_ARTIFACT_INPUT, createLockedSlot("Place Artifact Here", Material.DIAMOND_BLOCK));
        inventory.setItem(SLOT_DIAMOND_INPUT, createLockedSlot("Place Diamonds Here", Material.DIAMOND));
        inventory.setItem(SLOT_RESULT, createLockedSlot("Result", Material.BARRIER));
        inventory.setItem(SLOT_UPGRADE_BUTTON, createUpgradeButton());
    }

    private ItemStack createPlaceholderPane() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("");
            pane.setItemMeta(meta);
        }
        return pane;
    }

    private ItemStack createInputPlaceholder(String name, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&7" + name));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&8Place item here"));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createLockedSlot(String name, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&7" + name));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&8Place item here"));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        // Mark this as a locked placeholder (read-only)
        return item;
    }

    private ItemStack createUpgradeButton() {
        ItemStack item = new ItemStack(Material.ANVIL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&6&lUpgrade &eClick to upgrade"));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Consumes diamonds equal to cost"));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createUpgradeItem(EquippedArtifact equipped) {
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
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7" + set.getDisplayName()));
            lore.add("");
            lore.add(ChatColor.translateAlternateColorCodes('&', "&f&lStats:"));
            
            if (equipped.getStat("atk") > 0) {
                lore.add(ChatColor.translateAlternateColorCodes('&', "  &c+ATK: &f" + String.format("%.1f", equipped.getStat("atk")) + "%"));
            }
            if (equipped.getStat("defense") > 0) {
                lore.add(ChatColor.translateAlternateColorCodes('&', "  &9+DEF: &f" + String.format("%.1f", equipped.getStat("defense")) + "%"));
            }
            if (equipped.getStat("heal_regen") > 0) {
                lore.add(ChatColor.translateAlternateColorCodes('&', "  &a+HEAL: &f" + String.format("%.1f", equipped.getStat("heal_regen")) + "%"));
            }
            if (equipped.getStat("hunger") > 0) {
                lore.add(ChatColor.translateAlternateColorCodes('&', "  &e+HUNGER: &f" + String.format("%.1f", equipped.getStat("hunger")) + "%"));
            }
            
            lore.add("");
            if (equipped.canUpgrade()) {
                if (equipped.isNetheriteCost()) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&eUpgrade Cost: &f" + equipped.getUpgradeNetheriteCost() + " Netherite Ingot(s)"));
                } else {
                    lore.add(ChatColor.translateAlternateColorCodes('&', "&eUpgrade Cost: &f" + equipped.getUpgradeDiamondCost() + " Diamonds"));
                }
                lore.add(ChatColor.translateAlternateColorCodes('&', "&6Click to upgrade!"));
            } else {
                lore.add(ChatColor.translateAlternateColorCodes('&', "&cMax Level Reached"));
            }
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
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
