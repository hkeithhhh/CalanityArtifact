package com.calanity.artifact.model;

import java.util.List;

public class ArtifactPiece {
    
    private String type; // circlet, flower, plume, key, luminant
    private String displayName;
    private String material;
    private int price;
    private List<String> stats;
    private List<String> lore;
    private List<String> possibleStats; // atk, defense, heal_regen, hunger

    public ArtifactPiece(String type, String displayName, String material, int price,
                        List<String> stats, List<String> lore, List<String> possibleStats) {
        this.type = type;
        this.displayName = displayName;
        this.material = material;
        this.price = price;
        this.stats = stats;
        this.lore = lore;
        this.possibleStats = possibleStats;
    }

    public String getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMaterial() {
        return material;
    }

    public int getPrice() {
        return price;
    }

    public List<String> getStats() {
        return stats;
    }

    public List<String> getLore() {
        return lore;
    }

    public List<String> getPossibleStats() {
        return possibleStats;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
