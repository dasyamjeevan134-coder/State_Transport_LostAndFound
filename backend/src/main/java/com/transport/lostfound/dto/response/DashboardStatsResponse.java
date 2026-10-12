package com.transport.lostfound.dto.response;

public class DashboardStatsResponse {

    private long totalLostItems;
    private long openLostItems;
    private long totalFoundItems;
    private long availableFoundItems;
    private long pendingClaims;
    private long approvedClaims;
    private long totalReturns;

    public DashboardStatsResponse() {
    }

    public DashboardStatsResponse(long totalLostItems, long openLostItems, long totalFoundItems,
                                  long availableFoundItems, long pendingClaims, long approvedClaims,
                                  long totalReturns) {
        this.totalLostItems = totalLostItems;
        this.openLostItems = openLostItems;
        this.totalFoundItems = totalFoundItems;
        this.availableFoundItems = availableFoundItems;
        this.pendingClaims = pendingClaims;
        this.approvedClaims = approvedClaims;
        this.totalReturns = totalReturns;
    }

    public long getTotalLostItems() {
        return totalLostItems;
    }

    public void setTotalLostItems(long totalLostItems) {
        this.totalLostItems = totalLostItems;
    }

    public long getOpenLostItems() {
        return openLostItems;
    }

    public void setOpenLostItems(long openLostItems) {
        this.openLostItems = openLostItems;
    }

    public long getTotalFoundItems() {
        return totalFoundItems;
    }

    public void setTotalFoundItems(long totalFoundItems) {
        this.totalFoundItems = totalFoundItems;
    }

    public long getAvailableFoundItems() {
        return availableFoundItems;
    }

    public void setAvailableFoundItems(long availableFoundItems) {
        this.availableFoundItems = availableFoundItems;
    }

    public long getPendingClaims() {
        return pendingClaims;
    }

    public void setPendingClaims(long pendingClaims) {
        this.pendingClaims = pendingClaims;
    }

    public long getApprovedClaims() {
        return approvedClaims;
    }

    public void setApprovedClaims(long approvedClaims) {
        this.approvedClaims = approvedClaims;
    }

    public long getTotalReturns() {
        return totalReturns;
    }

    public void setTotalReturns(long totalReturns) {
        this.totalReturns = totalReturns;
    }
}
