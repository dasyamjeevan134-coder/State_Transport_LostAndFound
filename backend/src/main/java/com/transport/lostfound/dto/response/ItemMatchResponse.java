package com.transport.lostfound.dto.response;

import com.transport.lostfound.entity.ItemMatch;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ItemMatchResponse {

    private Long id;
    private Long lostItemId;
    private String lostItemName;
    private Long foundItemId;
    private String foundItemName;
    private BigDecimal matchScore;
    private String matchNotes;
    private LocalDateTime createdAt;

    public ItemMatchResponse() {
    }

    public static ItemMatchResponse fromEntity(ItemMatch match) {
        if (match == null) return null;
        ItemMatchResponse resp = new ItemMatchResponse();
        resp.setId(match.getId());
        if (match.getLostItem() != null) {
            resp.setLostItemId(match.getLostItem().getId());
            resp.setLostItemName(match.getLostItem().getItemName());
        }
        if (match.getFoundItem() != null) {
            resp.setFoundItemId(match.getFoundItem().getId());
            resp.setFoundItemName(match.getFoundItem().getItemName());
        }
        resp.setMatchScore(match.getMatchScore());
        resp.setMatchNotes(match.getMatchNotes());
        resp.setCreatedAt(match.getCreatedAt());
        return resp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLostItemId() {
        return lostItemId;
    }

    public void setLostItemId(Long lostItemId) {
        this.lostItemId = lostItemId;
    }

    public String getLostItemName() {
        return lostItemName;
    }

    public void setLostItemName(String lostItemName) {
        this.lostItemName = lostItemName;
    }

    public Long getFoundItemId() {
        return foundItemId;
    }

    public void setFoundItemId(Long foundItemId) {
        this.foundItemId = foundItemId;
    }

    public String getFoundItemName() {
        return foundItemName;
    }

    public void setFoundItemName(String foundItemName) {
        this.foundItemName = foundItemName;
    }

    public BigDecimal getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(BigDecimal matchScore) {
        this.matchScore = matchScore;
    }

    public String getMatchNotes() {
        return matchNotes;
    }

    public void setMatchNotes(String matchNotes) {
        this.matchNotes = matchNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
