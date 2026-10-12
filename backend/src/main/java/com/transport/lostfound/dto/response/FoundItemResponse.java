package com.transport.lostfound.dto.response;

import com.transport.lostfound.entity.FoundItem;
import com.transport.lostfound.enums.FoundItemStatus;
import com.transport.lostfound.enums.ItemCategory;
import com.transport.lostfound.enums.TransportType;

import java.time.LocalDateTime;

public class FoundItemResponse {

    private Long id;
    private String itemName;
    private ItemCategory category;
    private String description;
    private LocalDateTime foundDateTime;
    private TransportType transportType;
    private String routeNumber;
    private String location;
    private String photoUrl;
    private Long staffId;
    private String staffName;
    private FoundItemStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FoundItemResponse() {
    }

    public static FoundItemResponse fromEntity(FoundItem item) {
        if (item == null) return null;
        FoundItemResponse resp = new FoundItemResponse();
        resp.setId(item.getId());
        resp.setItemName(item.getItemName());
        resp.setCategory(item.getCategory());
        resp.setDescription(item.getDescription());
        resp.setFoundDateTime(item.getFoundDateTime());
        resp.setTransportType(item.getTransportType());
        resp.setRouteNumber(item.getRouteNumber());
        resp.setLocation(item.getLocation());
        resp.setPhotoUrl(item.getPhotoUrl());
        if (item.getStaff() != null) {
            resp.setStaffId(item.getStaff().getId());
            resp.setStaffName(item.getStaff().getFullName());
        }
        resp.setStatus(item.getStatus());
        resp.setCreatedAt(item.getCreatedAt());
        resp.setUpdatedAt(item.getUpdatedAt());
        return resp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public void setCategory(ItemCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getFoundDateTime() {
        return foundDateTime;
    }

    public void setFoundDateTime(LocalDateTime foundDateTime) {
        this.foundDateTime = foundDateTime;
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(TransportType transportType) {
        this.transportType = transportType;
    }

    public String getRouteNumber() {
        return routeNumber;
    }

    public void setRouteNumber(String routeNumber) {
        this.routeNumber = routeNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
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

    public FoundItemStatus getStatus() {
        return status;
    }

    public void setStatus(FoundItemStatus status) {
        this.status = status;
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
}
