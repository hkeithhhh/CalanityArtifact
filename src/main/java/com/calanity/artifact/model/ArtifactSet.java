package com.calanity.artifact.model;

import java.util.List;

public class ArtifactSet {
    
    private String id;
    private String displayName;
    private String description;
    private ArtifactPiece circlet;
    private ArtifactPiece flower;
    private ArtifactPiece plume;
    private ArtifactPiece key;
    private ArtifactPiece luminant;

    public ArtifactSet(String id, String displayName, String description,
                       ArtifactPiece circlet, ArtifactPiece flower, ArtifactPiece plume,
                       ArtifactPiece key, ArtifactPiece luminant) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.circlet = circlet;
        this.flower = flower;
        this.plume = plume;
        this.key = key;
        this.luminant = luminant;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public ArtifactPiece getCirclet() {
        return circlet;
    }

    public ArtifactPiece getFlower() {
        return flower;
    }

    public ArtifactPiece getPlume() {
        return plume;
    }

    public ArtifactPiece getKey() {
        return key;
    }

    public ArtifactPiece getLuminant() {
        return luminant;
    }

    public ArtifactPiece getPiece(String pieceType) {
        return switch(pieceType.toLowerCase()) {
            case "circlet" -> circlet;
            case "flower" -> flower;
            case "plume" -> plume;
            case "key" -> key;
            case "luminant" -> luminant;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
}
