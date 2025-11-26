package com.calanity.artifact.model;

import java.util.List;
import java.util.Map;

public class Artifact {
    
    private String id;
    private String displayName;
    private List<String> description;
    private String requiredItem;
    private int requiredItemCustomModel;
    private int tier2Count;
    private List<String> tier2Stats;
    private int tier5Count;
    private List<String> tier5Stats;
    private List<String> lore;

    public Artifact(String id, String displayName, List<String> description,
                   String requiredItem, int requiredItemCustomModel,
                   int tier2Count, List<String> tier2Stats,
                   int tier5Count, List<String> tier5Stats,
                   List<String> lore) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.requiredItem = requiredItem;
        this.requiredItemCustomModel = requiredItemCustomModel;
        this.tier2Count = tier2Count;
        this.tier2Stats = tier2Stats;
        this.tier5Count = tier5Count;
        this.tier5Stats = tier5Stats;
        this.lore = lore;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getDescription() {
        return description;
    }

    public String getRequiredItem() {
        return requiredItem;
    }

    public int getRequiredItemCustomModel() {
        return requiredItemCustomModel;
    }

    public int getTier2Count() {
        return tier2Count;
    }

    public List<String> getTier2Stats() {
        return tier2Stats;
    }

    public int getTier5Count() {
        return tier5Count;
    }

    public List<String> getTier5Stats() {
        return tier5Stats;
    }

    public List<String> getLore() {
        return lore;
    }

    @Override
    public String toString() {
        return "Artifact{" +
                "id='" + id + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
    }
}
