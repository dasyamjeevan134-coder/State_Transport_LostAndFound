package com.transport.lostfound.dto.response;

import com.transport.lostfound.entity.Claim;
import com.transport.lostfound.enums.ClaimStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ClaimResponse {

    private Long id;
    private Long foundItemId;
    private String foundItemName;
    private Long passengerId;
    private String passengerName;
    private String passengerEmail;
    private String passengerPhone;
    private String ownershipEvidence;
    private ClaimStatus claimStatus;
    private Long reviewerId;
    private String reviewerName;
    private String reviewNotes;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ClaimResponse() {
    }

    public static ClaimResponse fromEntity(Claim claim) {
        if (claim == null) return null;
        ClaimResponse resp = new ClaimResponse();
        resp.setId(claim.getId());
        if (claim.getFoundItem() != null) {
            resp.setFoundItemId(claim.getFoundItem().getId());
            resp.setFoundItemName(claim.getFoundItem().getItemName());
        }
        if (claim.getPassenger() != null) {
            resp.setPassengerId(claim.getPassenger().getId());
            resp.setPassengerName(claim.getPassenger().getFullName());
            resp.setPassengerEmail(claim.getPassenger().getEmail());
            resp.setPassengerPhone(claim.getPassenger().getPhone());
        }
        resp.setOwnershipEvidence(claim.getOwnershipEvidence());
        resp.setClaimStatus(claim.getClaimStatus());
        if (claim.getReviewer() != null) {
            resp.setReviewerId(claim.getReviewer().getId());
            resp.setReviewerName(claim.getReviewer().getFullName());
        }
        resp.setReviewNotes(claim.getReviewNotes());
        resp.setReviewedAt(claim.getReviewedAt());
        resp.setCreatedAt(claim.getCreatedAt());
        resp.setUpdatedAt(claim.getUpdatedAt());
        return resp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(Long passengerId) {
        this.passengerId = passengerId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getPassengerEmail() {
        return passengerEmail;
    }

    public void setPassengerEmail(String passengerEmail) {
        this.passengerEmail = passengerEmail;
    }

    public String getPassengerPhone() {
        return passengerPhone;
    }

    public void setPassengerPhone(String passengerPhone) {
        this.passengerPhone = passengerPhone;
    }

    public String getOwnershipEvidence() {
        return ownershipEvidence;
    }

    public void setOwnershipEvidence(String ownershipEvidence) {
        this.ownershipEvidence = ownershipEvidence;
    }

    public ClaimStatus getClaimStatus() {
        return claimStatus;
    }

    public void setClaimStatus(ClaimStatus claimStatus) {
        this.claimStatus = claimStatus;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getStatus() {
        return claimStatus != null ? claimStatus.name() : null;
    }

    public String getClaimDescription() {
        return ownershipEvidence;
    }

    public String getVerificationRemarks() {
        return reviewNotes;
    }

    public Map<String, Object> getFoundItem() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", foundItemId);
        map.put("itemName", foundItemName);
        map.put("routeNumber", "Transit Line");
        map.put("storageLocation", "Depot Locker Vault");
        map.put("foundDateTime", createdAt);
        return map;
    }

    public Map<String, Object> getClaimant() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", passengerId);
        map.put("fullName", passengerName != null ? passengerName : "Passenger");
        map.put("email", passengerEmail);
        map.put("phone", passengerPhone);
        return map;
    }

    public Map<String, Object> getVerifiedBy() {
        if (reviewerName == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("id", reviewerId);
        map.put("fullName", reviewerName);
        return map;
    }
}
