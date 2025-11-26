package com.calanity.artifact.gui;

import com.calanity.artifact.CalanityArtifactPlugin;
import com.calanity.artifact.model.ArtifactSet;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ArtifactBrowserGUI {

    private CalanityArtifactPlugin plugin;
    private Player player;
    private Inventory inventory;

    public ArtifactBrowserGUI(CalanityArtifactPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        createInventory();
    }

    private void createInventory() {
        int size = (int) Math.ceil(plugin.getArtifactManager().getAllArtifactSets().size() / 9.0) * 9;
        size = Math.max(9, Math.min(size, 54));
        
        inventory = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', "&c&lArtifact Sets"));
        fillArtifactSets();
    }

    private void fillArtifactSets() {
        int slot = 0;
        for (ArtifactSet set : plugin.getArtifactManager().getAllArtifactSets().values()) {
            if (slot >= inventory.getSize()) break;
            inventory.setItem(slot, createSetItem(set));
            slot++;
        }
    }

    private ItemStack createSetItem(ArtifactSet set) {
        ItemStack item = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', set.getDisplayName()));
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7" + set.getDescription()));
            lore.add("");
            lore.add(ChatColor.translateAlternateColorCodes('&', "&f5 Pieces:"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "  &e● &fCirclet"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "  &e● &fFlower"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "  &e● &fPlume"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "  &e● &fKey"));
            lore.add(ChatColor.translateAlternateColorCodes('&', "  &e● &fLuminant"));
            
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
