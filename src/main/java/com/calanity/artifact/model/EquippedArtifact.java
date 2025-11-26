package com.calanity.artifact.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.potion.PotionEffectType;
import java.lang.reflect.Method;

public class EquippedArtifact {
    
    private String setId;
    private String pieceType;
    private int level; // 1-20
    private Map<String, Double> stats; // atk, defense, heal_regen, hunger
    private Map<String, Double> potionEffects; // effectName -> potency (0.1 - 5.0)
    private static final List<String> POSSIBLE_EFFECTS = buildPossibleEffects();

    private static List<String> buildPossibleEffects() {
        List<String> out = new ArrayList<>();
        try {
            Method m = PotionEffectType.class.getMethod("values");
            Object arr = m.invoke(null);
            if (arr instanceof PotionEffectType[]) {
                for (PotionEffectType p : (PotionEffectType[]) arr) {
                    if (p != null && p.getName() != null) {
                        String name = p.getName();
                        // Strip "minecraft:" prefix if present (for 1.20.4+)
                        if (name.contains(":")) name = name.substring(name.indexOf(":") + 1);
                        out.add(name);
                    }
                }
                return out;
            }
        } catch (Exception ignored) {
        }

        // Fallback list covering common potion names (will be filtered later)
        String[] candidates = new String[] {
            "SPEED","SLOW","FAST_DIGGING","SLOW_DIGGING","INCREASE_DAMAGE","HEAL","HARM","JUMP","CONFUSION",
            "REGENERATION","DAMAGE_RESISTANCE","FIRE_RESISTANCE","WATER_BREATHING","INVISIBILITY","BLINDNESS","NIGHT_VISION",
            "HUNGER","WEAKNESS","POISON","WITHER","HEALTH_BOOST","ABSORPTION","SATURATION","GLOWING","LEVITATION",
            "LUCK","UNLUCK","SLOW_FALLING","CONDUIT_POWER","DOLPHINS_GRACE","BAD_OMEN","HERO_OF_THE_VILLAGE"
        };
        for (String s : candidates) {
            PotionEffectType t = PotionEffectType.getByName(s);
            if (t != null && t.getName() != null) {
                String name = t.getName();
                // Strip "minecraft:" prefix if present
                if (name.contains(":")) name = name.substring(name.indexOf(":") + 1);
                out.add(name);
            }
        }
        return out;
    }

    // Disallow a set of clearly negative effects except those explicitly allowed
    private static final java.util.Set<String> DISALLOWED_NEGATIVE = java.util.Set.of(
        "POISON","BLINDNESS","CONFUSION","SLOW","SLOW_DIGGING","WEAKNESS","HARM","UNLUCK","BAD_OMEN","DARKNESS"
    );

    public static List<String> getAllPossibleEffectNames() {
        return POSSIBLE_EFFECTS;
    }

    public EquippedArtifact(String setId, String pieceType) {
        this.setId = setId;
        this.pieceType = pieceType;
        this.level = 1;
        this.stats = new HashMap<>();
        this.potionEffects = new HashMap<>();
        randomizeStats();
    }

    public EquippedArtifact(String setId, String pieceType, int level, Map<String, Double> stats) {
        this(setId, pieceType, level, stats, new HashMap<>());
    }

    public EquippedArtifact(String setId, String pieceType, int level, Map<String, Double> stats, Map<String, Double> effects) {
        this.setId = setId;
        this.pieceType = pieceType;
        this.level = level;
        this.stats = stats != null ? stats : new HashMap<>();
        this.potionEffects = effects != null ? effects : new HashMap<>();
    }

    private void randomizeStats() {
        Random rand = new Random();
        // Initialize with 0
        stats.put("atk", 0.0);
        stats.put("defense", 0.0);
        stats.put("heal_regen", 0.0);
        stats.put("hunger", 0.0);
        
        // Randomly assign base stats (max 60% of max)
        String[] statTypes = {"atk", "defense", "heal_regen", "hunger"};
        for (String stat : statTypes) {
            if (rand.nextDouble() < 0.5) {
                double value = rand.nextDouble() * 60.0 * 0.2; // 0-12 for level 1
                stats.put(stat, Math.round(value * 100.0) / 100.0);
            }
        }

        // start with no potion effects by default
        potionEffects.clear();
    }

    public void upgrade() {
        if (level < 20) {
            level++;
            Random rand = new Random();

            // If level <=5 we attempt to add new potion effects (1-3 per upgrade, capped at 3 total)
            if (level <= 5) {
                int currently = potionEffects.size();
                int remaining = Math.max(0, 3 - currently);
                if (remaining > 0) {
                    int toAdd = 1 + rand.nextInt(3); // 1-3
                    toAdd = Math.min(toAdd, remaining);
                    List<String> pool = new ArrayList<>(POSSIBLE_EFFECTS);
                    pool.removeAll(potionEffects.keySet());
                    // Remove explicitly disallowed negative effects; allow WITHER/HUNGER
                    pool.removeIf(n -> n == null ? false : DISALLOWED_NEGATIVE.contains(n.toUpperCase()));
                    for (int i = 0; i < toAdd && !pool.isEmpty(); i++) {
                        String pick = pool.remove(rand.nextInt(pool.size()));
                        double potency = 0.1 + rand.nextDouble() * 0.9; // 0.1 - 1.0
                        potency = Math.round(potency * 10.0) / 10.0;
                        potionEffects.put(pick, potency);
                    }
                }
            } else {
                // After effects locked (level >5) randomly upgrade existing effects' potency
                if (!potionEffects.isEmpty()) {
                    int picks = 1 + rand.nextInt(Math.min(2, potionEffects.size()));
                    List<String> keys = new ArrayList<>(potionEffects.keySet());
                    for (int i = 0; i < picks; i++) {
                        String k = keys.get(rand.nextInt(keys.size()));
                        double current = potionEffects.getOrDefault(k, 0.0);
                        double inc = 0.1 + rand.nextDouble() * 0.5; // increase 0.1 - 0.6
                        double nv = Math.min(5.0, Math.round((current + inc) * 10.0) / 10.0);
                        potionEffects.put(k, nv);
                    }
                } else {
                    // fallback: increase numeric stats as before
                    String[] statTypes = {"atk", "defense", "heal_regen", "hunger"};
                    String stat = statTypes[rand.nextInt(statTypes.length)];
                    double currentValue = stats.getOrDefault(stat, 0.0);
                    double maxValue = 60.0 * (level / 20.0); // Max 60% based on level
                    double increase = 0.1 + rand.nextDouble() * 5.0; // 0.1 - 5.0
                    double newValue = Math.min(currentValue + increase, maxValue);
                    stats.put(stat, Math.round(newValue * 100.0) / 100.0);
                }
            }
        }
    }

    public Map<String, Double> getPotionEffects() {
        return potionEffects;
    }

    public void setPotionEffects(Map<String, Double> effects) {
        this.potionEffects = effects != null ? effects : new HashMap<>();
    }

    public String getSetId() {
        return setId;
    }

    public String getPieceType() {
        return pieceType;
    }

    public int getLevel() {
        return level;
    }

    public Map<String, Double> getStats() {
        return stats;
    }

    public double getStat(String statType) {
        return stats.getOrDefault(statType, 0.0);
    }

    public boolean canUpgrade() {
        return level < 20;
    }

    public int getUpgradeCost() {
        // Backwards-compatible: return diamond cost when applicable, otherwise 0
        if (level >= 1 && level <= 15) return getUpgradeDiamondCost();
        return 0;
    }

    // Diamond cost for levels 1..15 scaled from 10 -> 64
    public int getUpgradeDiamondCost() {
        if (level < 1) return 10;
        if (level > 15) return 0;
        int min = 10;
        int max = 64;
        // linear interpolation across 1..15
        double frac = (double) (level - 1) / (15 - 1);
        int cost = (int) Math.round(min + frac * (max - min));
        return Math.max(1, cost);
    }

    // Netherite cost for levels 16..20: 1 netherite each
    public int getUpgradeNetheriteCost() {
        if (level >= 16 && level <= 20) return 1;
        return 0;
    }

    public boolean isNetheriteCost() {
        return level >= 16 && level <= 20;
    }
}
