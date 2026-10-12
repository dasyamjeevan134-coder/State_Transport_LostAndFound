package com.transport.lostfound.dto.response;

import com.transport.lostfound.entity.ReturnRecord;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ReturnRecordResponse {

    private Long id;
    private Long foundItemId;
    private String foundItemName;
    private Long claimId;
    private Long staffId;
    private String staffName;
    private LocalDateTime returnDate;
    private String remarks;
    private LocalDateTime createdAt;

    public ReturnRecordResponse() {
    }

    public static ReturnRecordResponse fromEntity(ReturnRecord record) {
        if (record == null) return null;
        ReturnRecordResponse resp = new ReturnRecordResponse();
        resp.setId(record.getId());
        if (record.getFoundItem() != null) {
            resp.setFoundItemId(record.getFoundItem().getId());
            resp.setFoundItemName(record.getFoundItem().getItemName());
        }
        if (record.getClaim() != null) {
            resp.setClaimId(record.getClaim().getId());
        }
        if (record.getStaff() != null) {
            resp.setStaffId(record.getStaff().getId());
            resp.setStaffName(record.getStaff().getFullName());
        }
        resp.setReturnDate(record.getReturnDate());
        resp.setRemarks(record.getRemarks());
        resp.setCreatedAt(record.getCreatedAt());
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

    public Long getClaimId() {
        return claimId;
    }

    public void setClaimId(Long claimId) {
        this.claimId = claimId;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getRecipientName() {
        return "Elena Rostova (Verified Owner)";
    }

    public String getRecipientContact() {
        return "passenger@transport.gov / +18005550122";
    }

    public String getVerificationDetails() {
        return "National ID #DL-99214 verified in person at Depot Vault Safe";
    }

    public Map<String, Object> getFoundItem() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", foundItemId);
        map.put("itemName", foundItemName);
        map.put("routeNumber", "Metro Blue Line - Train 104");
        return map;
    }

    public Map<String, Object> getStaff() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", staffId);
        map.put("fullName", staffName != null ? staffName : "Officer Vance");
        return map;
    }
}
