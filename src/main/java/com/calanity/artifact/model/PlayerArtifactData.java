package com.calanity.artifact.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerArtifactData {

    private UUID playerUUID;
    private Map<String, EquippedArtifact> equippedPieces; // slot (circlet/flower/plume/key/luminant) -> EquippedArtifact

    public PlayerArtifactData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.equippedPieces = new HashMap<>();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public Map<String, EquippedArtifact> getEquippedPieces() {
        return equippedPieces;
    }

    public void equipPiece(String pieceType, EquippedArtifact artifact) {
        equippedPieces.put(pieceType.toLowerCase(), artifact);
    }

    public void unequipPiece(String pieceType) {
        equippedPieces.remove(pieceType.toLowerCase());
    }

    public EquippedArtifact getEquippedArtifact(String pieceType) {
        return equippedPieces.get(pieceType.toLowerCase());
    }

    public boolean hasPieceEquipped(String pieceType) {
        return equippedPieces.containsKey(pieceType.toLowerCase());
    }

    public int getEquippedCount() {
        return equippedPieces.size();
    }

    public void clearAll() {
        equippedPieces.clear();
    }
}
