package com.calanity.artifact.manager;

import com.calanity.artifact.CalanityArtifactPlugin;
import com.calanity.artifact.model.PlayerArtifactData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Player;
import java.util.UUID;

public class PlayerArtifactManager {

    private CalanityArtifactPlugin plugin;
    private Map<UUID, PlayerArtifactData> playerData;
    private File dataFolder;

    public PlayerArtifactManager(CalanityArtifactPlugin plugin) {
        this.plugin = plugin;
        this.playerData = new HashMap<>();
        this.dataFolder = new File(plugin.getDataFolder(), "players");

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        loadAllPlayers();
    }

    public PlayerArtifactData getPlayerData(UUID playerUUID) {
        return playerData.computeIfAbsent(playerUUID, k -> loadOrCreatePlayer(playerUUID));
    }

    public PlayerArtifactData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    private PlayerArtifactData loadOrCreatePlayer(UUID playerUUID) {
        File playerFile = new File(dataFolder, playerUUID + ".yml");

        if (playerFile.exists()) {
            return loadPlayerFromFile(playerFile);
        } else {
            return new PlayerArtifactData(playerUUID);
        }
    }

    private PlayerArtifactData loadPlayerFromFile(File file) {
        UUID playerUUID = UUID.fromString(file.getName().replace(".yml", ""));
        PlayerArtifactData data = new PlayerArtifactData(playerUUID);
        try {
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            if (cfg == null) return data;

            if (cfg.isConfigurationSection("equipped_pieces")) {
                for (String pieceKey : cfg.getConfigurationSection("equipped_pieces").getKeys(false)) {
                    String path = "equipped_pieces." + pieceKey;
                    String setId = cfg.getString(path + ".set_id", null);
                    int level = cfg.getInt(path + ".level", 1);

                    java.util.Map<String, Double> stats = new java.util.HashMap<>();
                    stats.put("atk", cfg.getDouble(path + ".stats.atk", 0.0));
                    stats.put("defense", cfg.getDouble(path + ".stats.defense", 0.0));
                    stats.put("heal_regen", cfg.getDouble(path + ".stats.heal_regen", 0.0));
                    stats.put("hunger", cfg.getDouble(path + ".stats.hunger", 0.0));

                    java.util.Map<String, Double> effects = new java.util.HashMap<>();
                    if (cfg.isConfigurationSection(path + ".effects")) {
                        for (String effKey : cfg.getConfigurationSection(path + ".effects").getKeys(false)) {
                            effects.put(effKey, cfg.getDouble(path + ".effects." + effKey, 0.0));
                        }
                    }

                    if (setId != null) {
                        com.calanity.artifact.model.EquippedArtifact eq = new com.calanity.artifact.model.EquippedArtifact(setId, pieceKey, level, stats, effects);
                        data.equipPiece(pieceKey, eq);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load player data for " + file.getName() + ": " + e.getMessage());
        }

        return data;
    }

    public void savePlayer(UUID playerUUID) {
        PlayerArtifactData data = playerData.get(playerUUID);
        if (data != null) {
            File playerFile = new File(dataFolder, playerUUID + ".yml");
            try {
                FileConfiguration cfg = new YamlConfiguration();
                cfg.set("# generated", "Artifact data for " + playerUUID);

                for (var entry : data.getEquippedPieces().entrySet()) {
                    String pieceKey = entry.getKey();
                    com.calanity.artifact.model.EquippedArtifact eq = entry.getValue();
                    String base = "equipped_pieces." + pieceKey;
                    cfg.set(base + ".set_id", eq.getSetId());
                    cfg.set(base + ".level", eq.getLevel());
                    cfg.set(base + ".stats.atk", eq.getStat("atk"));
                    cfg.set(base + ".stats.defense", eq.getStat("defense"));
                    cfg.set(base + ".stats.heal_regen", eq.getStat("heal_regen"));
                    cfg.set(base + ".stats.hunger", eq.getStat("hunger"));
                    if (eq.getPotionEffects() != null && !eq.getPotionEffects().isEmpty()) {
                        for (var eff : eq.getPotionEffects().entrySet()) {
                            cfg.set(base + ".effects." + eff.getKey(), eff.getValue());
                        }
                    }
                }

                cfg.save(playerFile);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save player data for " + playerUUID + ": " + e.getMessage());
            }
        }
    }

    public void saveAllPlayers() {
        for (UUID uuid : playerData.keySet()) {
            savePlayer(uuid);
        }
    }

    public void applyEffects(Player player) {
        PlayerArtifactData data = getPlayerData(player.getUniqueId());
        if (data == null) return;

        double totalAtk = 0.0;
        double totalDef = 0.0;
        double totalHeal = 0.0;
        double totalHun = 0.0;

        for (var entry : data.getEquippedPieces().entrySet()) {
            var eq = entry.getValue();
            if (eq == null) continue;
            totalAtk += eq.getStat("atk");
            totalDef += eq.getStat("defense");
            totalHeal += eq.getStat("heal_regen");
            totalHun += eq.getStat("hunger");
        }

        // Attack: apply as ADD_SCALAR on GENERIC_ATTACK_DAMAGE (percent -> decimal)
        AttributeInstance atkInst = null;
        try {
            atkInst = player.getAttribute(Attribute.valueOf("GENERIC_ATTACK_DAMAGE"));
        } catch (Exception ignored) {}
        if (atkInst != null) {
            // remove existing modifiers named artifact_atk
            removeAttributeModifiersByName(atkInst, "artifact_atk");
            if (totalAtk > 0.0) {
                UUID id = UUID.nameUUIDFromBytes((player.getUniqueId().toString() + "-atk").getBytes());
                AttributeModifier mod = new AttributeModifier(id, "artifact_atk", totalAtk / 100.0, AttributeModifier.Operation.ADD_SCALAR);
                atkInst.addModifier(mod);
            }
        }

        // Defense: apply as ADD_SCALAR on GENERIC_ARMOR
        AttributeInstance defInst = null;
        try {
            defInst = player.getAttribute(Attribute.valueOf("GENERIC_ARMOR"));
        } catch (Exception ignored) {}
        if (defInst != null) {
            removeAttributeModifiersByName(defInst, "artifact_def");
            if (totalDef > 0.0) {
                UUID id = UUID.nameUUIDFromBytes((player.getUniqueId().toString() + "-def").getBytes());
                AttributeModifier mod = new AttributeModifier(id, "artifact_def", totalDef / 100.0, AttributeModifier.Operation.ADD_SCALAR);
                defInst.addModifier(mod);
            }
        }

        // Aggregate potion effects from all equipped pieces
        java.util.Map<String, Double> aggEffects = new java.util.HashMap<>();
        for (var entry : data.getEquippedPieces().entrySet()) {
            var eq = entry.getValue();
            if (eq == null) continue;
            var pe = eq.getPotionEffects();
            if (pe == null) continue;
            for (var e : pe.entrySet()) {
                aggEffects.merge(e.getKey(), e.getValue(), Double::sum);
            }
        }

        // Convert numeric heal_regen stat into a regeneration potion effect
        if (totalHeal > 0.0) {
            // Map percent-ish heal_regen to potency: divide by 10 so small values grant visible Regen I
            double regenPotency = Math.round((totalHeal / 10.0) * 10.0) / 10.0;
            if (regenPotency <= 0) regenPotency = 0.1;
            aggEffects.merge("REGENERATION", regenPotency, Double::sum);
        }

        // Remove previously applied possible effects
        for (String effName : com.calanity.artifact.model.EquippedArtifact.getAllPossibleEffectNames()) {
            try {
                var pet = PotionEffectType.getByName(effName);
                if (pet != null) player.removePotionEffect(pet);
            } catch (Exception ignored) {}
        }

        // Apply aggregated effects
        for (var e : aggEffects.entrySet()) {
            String name = e.getKey();
            double potency = e.getValue();
            // Try with and without minecraft: prefix (for compatibility with different versions)
            PotionEffectType pet = PotionEffectType.getByName(name);
            if (pet == null) pet = PotionEffectType.getByName("minecraft:" + name);
            if (pet == null) continue;
            // Map potency to amplifier: 0.1-1.0 → level I (amp 0), 1.0-2.0 → level II (amp 1), etc.
            int amp = Math.max(0, (int) Math.ceil(potency) - 1);
            try {
                player.addPotionEffect(new PotionEffect(pet, Integer.MAX_VALUE, amp, true, false, true));
            } catch (Exception ignored) {}
        }
    }

    private void removeAttributeModifiersByName(AttributeInstance inst, String name) {
        try {
            var mods = inst.getModifiers();
            java.util.List<AttributeModifier> toRemove = new java.util.ArrayList<>();
            for (AttributeModifier m : mods) {
                if (m.getName() != null && m.getName().equals(name)) toRemove.add(m);
            }
            for (AttributeModifier m : toRemove) inst.removeModifier(m);
        } catch (Exception ignored) {}
    }

    private void loadAllPlayers() {
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                try {
                    UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
                    playerData.put(uuid, loadPlayerFromFile(file));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid player file: " + file.getName());
                }
            }
        }
    }
}
