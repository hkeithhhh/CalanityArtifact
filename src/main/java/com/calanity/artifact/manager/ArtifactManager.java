package com.calanity.artifact.manager;

import com.calanity.artifact.CalanityArtifactPlugin;
import com.calanity.artifact.model.ArtifactSet;
import com.calanity.artifact.model.ArtifactPiece;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArtifactManager {

    private CalanityArtifactPlugin plugin;
    private Map<String, ArtifactSet> artifactSets;

    public ArtifactManager(CalanityArtifactPlugin plugin) {
        this.plugin = plugin;
        this.artifactSets = new HashMap<>();
        loadArtifactSets();
    }

    public void loadArtifactSets() {
        artifactSets.clear();
        FileConfiguration config = plugin.getConfig();

        ConfigurationSection setsSection = config.getConfigurationSection("artifact_sets");
        if (setsSection != null) {
            for (String setKey : setsSection.getKeys(false)) {
                ConfigurationSection setSection = setsSection.getConfigurationSection(setKey);
                if (setSection != null) {
                    ArtifactSet set = loadArtifactSet(setKey, setSection);
                    artifactSets.put(setKey, set);
                }
            }
        }

        plugin.getLogger().info("Loaded " + artifactSets.size() + " artifact sets");
    }

    private ArtifactSet loadArtifactSet(String id, ConfigurationSection section) {
        String displayName = section.getString("display_name", "Unknown");
        String description = section.getString("description", "");

        ArtifactPiece circlet = loadPiece("circlet", section.getConfigurationSection("circlet"));
        ArtifactPiece flower = loadPiece("flower", section.getConfigurationSection("flower"));
        ArtifactPiece plume = loadPiece("plume", section.getConfigurationSection("plume"));
        ArtifactPiece key = loadPiece("key", section.getConfigurationSection("key"));
        ArtifactPiece luminant = loadPiece("luminant", section.getConfigurationSection("luminant"));

        return new ArtifactSet(id, displayName, description, circlet, flower, plume, key, luminant);
    }

    private ArtifactPiece loadPiece(String type, ConfigurationSection section) {
        if (section == null) return null;
        
        String displayName = section.getString("display_name", type);
        String material = section.getString("material", "DIAMOND");
        int price = section.getInt("price", 0);
        List<String> stats = section.getStringList("stats");
        List<String> lore = section.getStringList("lore");
        List<String> possibleStats = section.getStringList("possible_stats");
        if (possibleStats.isEmpty()) {
            possibleStats = List.of("atk", "defense", "heal_regen", "hunger");
        }

        return new ArtifactPiece(type, displayName, material, price, stats, lore, possibleStats);
    }

    public ArtifactSet getArtifactSet(String id) {
        return artifactSets.get(id);
    }

    public Map<String, ArtifactSet> getAllArtifactSets() {
        return artifactSets;
    }
}
