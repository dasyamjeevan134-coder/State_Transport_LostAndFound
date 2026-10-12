package com.transport.lostfound.dto.request;

import com.transport.lostfound.enums.ClaimStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ClaimStatusUpdateRequest {

    @NotNull(message = "Claim status is required")
    private ClaimStatus status;

    @Size(max = 2000, message = "Review notes cannot exceed 2000 characters")
    private String reviewNotes;

    public ClaimStatusUpdateRequest() {
    }

    public ClaimStatusUpdateRequest(ClaimStatus status, String reviewNotes) {
        this.status = status;
        this.reviewNotes = reviewNotes;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }
}
