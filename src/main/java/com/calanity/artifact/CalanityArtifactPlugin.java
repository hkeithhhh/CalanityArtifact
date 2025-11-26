package com.calanity.artifact;

import org.bukkit.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import com.calanity.artifact.model.EquippedArtifact;
import com.calanity.artifact.model.ArtifactPiece;
import com.calanity.artifact.model.ArtifactSet;
import java.util.ArrayList;
import java.util.List;
import com.calanity.artifact.commands.ArtifactCommand;
import com.calanity.artifact.commands.ArtifactAdminCommand;
import com.calanity.artifact.commands.ProfileCommand;
import com.calanity.artifact.commands.UpgradeCommand;
import com.calanity.artifact.listener.PlayerInteractListener;
import com.calanity.artifact.listener.InventoryClickListener;
import com.calanity.artifact.manager.ArtifactManager;
import com.calanity.artifact.manager.PlayerArtifactManager;

public class CalanityArtifactPlugin extends JavaPlugin {

    private static CalanityArtifactPlugin instance;
    private ArtifactManager artifactManager;
    private PlayerArtifactManager playerArtifactManager;
    private NamespacedKey artifactKey;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        try {
            // Initialize managers
            artifactManager = new ArtifactManager(this);
            playerArtifactManager = new PlayerArtifactManager(this);

            artifactKey = new NamespacedKey(this, "artifact_data");

            // Register commands safely
            registerCommand("artifacts", new ArtifactCommand(this));
            registerCommand("artifactadmin", new ArtifactAdminCommand(this));
            registerCommand("profile", new ProfileCommand(this));
            registerCommand("upgrade", new UpgradeCommand(this));
            registerCommand("giveartifact", new com.calanity.artifact.commands.GiveArtifactCommand(this));

            // Register listeners
            getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
            getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
            // (auto-equip listener removed)

            // Apply effects for online players (in case they have saved equips)
            for (org.bukkit.entity.Player p : getServer().getOnlinePlayers()) {
                try {
                    playerArtifactManager.applyEffects(p);
                } catch (Exception ignored) {}
            }

            getLogger().info("✓ CalanityArtifact enabled successfully!");
        } catch (Exception e) {
            getLogger().severe("Failed to enable CalanityArtifact!");
            e.printStackTrace();
            getPluginLoader().disablePlugin(this);
        }
    }

    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor) {
        var cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(executor);
        } else {
            getLogger().warning("Command not found in plugin.yml: " + name);
        }
    }

    @Override
    public void onDisable() {
        if (playerArtifactManager != null) {
            playerArtifactManager.saveAllPlayers();
        }
        getLogger().info("CalanityArtifact has been disabled!");
    }

    public static CalanityArtifactPlugin getInstance() {
        return instance;
    }

    public ArtifactManager getArtifactManager() {
        return artifactManager;
    }

    public PlayerArtifactManager getPlayerArtifactManager() {
        return playerArtifactManager;
    }

    public void reloadConfigs() {
        reloadConfig();
        artifactManager.loadArtifactSets();
        getLogger().info("Configs reloaded!");
    }

    public ItemStack toItemStack(EquippedArtifact eq) {
        ArtifactSet set = artifactManager.getArtifactSet(eq.getSetId());
        ArtifactPiece piece = null;
        if (set != null) piece = set.getPiece(eq.getPieceType());

        Material mat = Material.DIAMOND;
        String display = capitalize(eq.getPieceType());
        if (piece != null) {
            Material m = Material.matchMaterial(piece.getMaterial());
            if (m != null) mat = m;
            display = piece.getDisplayName();
        }

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6" + display + " §8[Lvl " + eq.getLevel() + "/20] ");
            List<String> lore = new ArrayList<>();
            lore.add("§7" + (set != null ? set.getDisplayName() : "Unknown") + " - " + capitalize(eq.getPieceType()));
            lore.add("");
            if (eq.getStat("atk") > 0) lore.add("§c+ATK: §f" + String.format("%.1f", eq.getStat("atk")) + "%");
            if (eq.getStat("defense") > 0) lore.add("§9+DEF: §f" + String.format("%.1f", eq.getStat("defense")) + "%");
            if (eq.getStat("heal_regen") > 0) lore.add("§a+HEAL: §f" + String.format("%.1f", eq.getStat("heal_regen")) + "%");
            if (eq.getStat("hunger") > 0) lore.add("§e+HUNGER: §f" + String.format("%.1f", eq.getStat("hunger")) + "%");
                // Show potion effects in lore
                if (eq.getPotionEffects() != null && !eq.getPotionEffects().isEmpty()) {
                    lore.add("");
                    lore.add("§6Effects:");
                    for (var e : eq.getPotionEffects().entrySet()) {
                        String name = e.getKey();
                        double pot = e.getValue();
                        lore.add("§b" + name + ": §f" + String.format("%.1f", pot));
                    }
                }

            meta.setLore(lore);

            // Persist data in PDC: setId|pieceType|level|atk=val,def=val,heal=val,hunger=val
            StringBuilder sb = new StringBuilder();
            sb.append(eq.getSetId()).append("|").append(eq.getPieceType()).append("|").append(eq.getLevel()).append("|");
            sb.append("atk=").append(eq.getStat("atk")).append(",");
            sb.append("def=").append(eq.getStat("defense")).append(",");
            sb.append("heal=").append(eq.getStat("heal_regen")).append(",");
            sb.append("hun=").append(eq.getStat("hunger")).append(";");
            // serialize potion effects as name:pot|name:pot
            var pe = eq.getPotionEffects();
            if (pe != null && !pe.isEmpty()) {
                StringBuilder es = new StringBuilder();
                boolean first = true;
                for (var e : pe.entrySet()) {
                    if (!first) es.append("|");
                    es.append(e.getKey()).append(":").append(e.getValue());
                    first = false;
                }
                sb.append("effects=").append(es.toString());
            }
            meta.getPersistentDataContainer().set(artifactKey, PersistentDataType.STRING, sb.toString());

            item.setItemMeta(meta);
        }
        return item;
    }

    public EquippedArtifact fromItemStack(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        if (meta.getPersistentDataContainer().has(artifactKey, PersistentDataType.STRING)) {
            String s = meta.getPersistentDataContainer().get(artifactKey, PersistentDataType.STRING);
            if (s == null) return null;
            // parse format: setId|pieceType|level|atk=val,def=val,heal=val,hun=val
            String[] parts = s.split("\\|", 4);
            if (parts.length < 4) return null;
            String setId = parts[0];
            String pieceType = parts[1];
            int level = 1;
            try { level = Integer.parseInt(parts[2]); } catch (Exception ignored) {}
            String statsPart = parts[3];
            java.util.Map<String, Double> stats = new java.util.HashMap<>();
            stats.put("atk", 0.0);
            stats.put("defense", 0.0);
            stats.put("heal_regen", 0.0);
            stats.put("hunger", 0.0);

            java.util.Map<String, Double> effects = new java.util.HashMap<>();

            // statsPart might contain ";" separating stats and effects
            String[] statAndEffects = statsPart.split(";", 2);
            String statStr = statAndEffects[0];
            String effStr = statAndEffects.length > 1 ? statAndEffects[1] : null;

            String[] kv = statStr.split(",");
            for (String kvp : kv) {
                String[] kvs = kvp.split("=", 2);
                if (kvs.length < 2) continue;
                switch (kvs[0]) {
                    case "atk": stats.put("atk", Double.parseDouble(kvs[1])); break;
                    case "def": stats.put("defense", Double.parseDouble(kvs[1])); break;
                    case "heal": stats.put("heal_regen", Double.parseDouble(kvs[1])); break;
                    case "hun": stats.put("hunger", Double.parseDouble(kvs[1])); break;
                }
            }

            if (effStr != null && effStr.startsWith("effects=")) {
                String list = effStr.substring("effects=".length());
                String[] items = list.split("\\|");
                for (String it : items) {
                    String[] kv2 = it.split(":", 2);
                    if (kv2.length < 2) continue;
                    try { effects.put(kv2[0], Double.parseDouble(kv2[1])); } catch (Exception ignored) {}
                }
            }

            return new EquippedArtifact(setId, pieceType, level, stats, effects);
        }

        // Fallback: attempt to parse from lore/display (backwards compatibility)
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            if (lore != null && !lore.isEmpty()) {
                String first = lore.get(0).replaceAll("§.", "").trim();
                if (first.contains(" - ")) {
                    String[] pr = first.split(" - ", 2);
                    String setDisplay = pr[0].trim();
                    String pieceName = pr[1].trim();
                    String pieceType = pieceName.toLowerCase();

                    String setId = null;
                    for (var e : artifactManager.getAllArtifactSets().entrySet()) {
                        if (e.getValue().getDisplayName().equalsIgnoreCase(setDisplay)) {
                            setId = e.getKey(); break;
                        }
                    }
                    if (setId == null) return null;

                    int level = 1;
                    String display = meta.hasDisplayName() ? meta.getDisplayName().replaceAll("§.", "") : "";
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("Lvl (\\d+)").matcher(display);
                    if (m.find()) {
                        try { level = Integer.parseInt(m.group(1)); } catch (Exception ignored) {}
                    }

                    java.util.Map<String, Double> stats = new java.util.HashMap<>();
                    stats.put("atk", 0.0); stats.put("defense", 0.0); stats.put("heal_regen", 0.0); stats.put("hunger", 0.0);
                    for (String line : lore) {
                        String clean = line.replaceAll("§.", "");
                        if (clean.toLowerCase().contains("atk")) {
                            java.util.regex.Matcher mm = java.util.regex.Pattern.compile("([0-9]+\\.?[0-9]*)").matcher(clean);
                            if (mm.find()) stats.put("atk", Double.parseDouble(mm.group(1)));
                        }
                        if (clean.toLowerCase().contains("def")) {
                            java.util.regex.Matcher mm = java.util.regex.Pattern.compile("([0-9]+\\.?[0-9]*)").matcher(clean);
                            if (mm.find()) stats.put("defense", Double.parseDouble(mm.group(1)));
                        }
                        if (clean.toLowerCase().contains("heal")) {
                            java.util.regex.Matcher mm = java.util.regex.Pattern.compile("([0-9]+\\.?[0-9]*)").matcher(clean);
                            if (mm.find()) stats.put("heal_regen", Double.parseDouble(mm.group(1)));
                        }
                        if (clean.toLowerCase().contains("hunger")) {
                            java.util.regex.Matcher mm = java.util.regex.Pattern.compile("([0-9]+\\.?[0-9]*)").matcher(clean);
                            if (mm.find()) stats.put("hunger", Double.parseDouble(mm.group(1)));
                        }
                    }

                    return new EquippedArtifact(setId, pieceType, level, stats);
                }
            }
        }

        return null;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }
}
