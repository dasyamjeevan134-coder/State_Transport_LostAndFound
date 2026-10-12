package com.transport.lostfound.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ReturnRecordCreateRequest {

    @NotNull(message = "Found item ID is required")
    private Long foundItemId;

    @NotNull(message = "Claim ID is required")
    private Long claimId;

    @PastOrPresent(message = "Return date cannot be in the future")
    private LocalDateTime returnDate;

    @Size(max = 2000, message = "Remarks cannot exceed 2000 characters")
    private String remarks;

    public ReturnRecordCreateRequest() {
    }

    public ReturnRecordCreateRequest(Long foundItemId, Long claimId, LocalDateTime returnDate, String remarks) {
        this.foundItemId = foundItemId;
        this.claimId = claimId;
        this.returnDate = returnDate;
        this.remarks = remarks;
    }

    public Long getFoundItemId() {
        return foundItemId;
    }

    public void setFoundItemId(Long foundItemId) {
        this.foundItemId = foundItemId;
    }

    public Long getClaimId() {
        return claimId;
    }

    public void setClaimId(Long claimId) {
        this.claimId = claimId;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
