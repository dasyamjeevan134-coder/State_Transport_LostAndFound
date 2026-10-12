package com.transport.lostfound.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ClaimCreateRequest {

    @NotNull(message = "Found item ID is required")
    private Long foundItemId;

    @NotBlank(message = "Ownership evidence is required")
    @Size(min = 10, max = 3000, message = "Ownership evidence must be between 10 and 3000 characters")
    private String ownershipEvidence;

    public ClaimCreateRequest() {
    }

    public ClaimCreateRequest(Long foundItemId, String ownershipEvidence) {
        this.foundItemId = foundItemId;
        this.ownershipEvidence = ownershipEvidence;
    }

    public Long getFoundItemId() {
        return foundItemId;
    }

    public void setFoundItemId(Long foundItemId) {
        this.foundItemId = foundItemId;
    }

    public String getOwnershipEvidence() {
        return ownershipEvidence;
    }

    public void setOwnershipEvidence(String ownershipEvidence) {
        this.ownershipEvidence = ownershipEvidence;
    }
}
