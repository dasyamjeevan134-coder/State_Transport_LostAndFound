package com.transport.lostfound.dto.response;

import com.transport.lostfound.entity.LostItem;
import com.transport.lostfound.enums.ItemCategory;
import com.transport.lostfound.enums.LostItemStatus;
import com.transport.lostfound.enums.TransportType;

import java.time.LocalDateTime;

public class LostItemResponse {

    private Long id;
    private String itemName;
    private ItemCategory category;
    private String description;
    private LocalDateTime lossDateTime;
    private TransportType transportType;
    private String routeNumber;
    private String location;
    private String photoUrl;
    private Long passengerId;
    private String passengerName;
    private String passengerPhone;
    private LostItemStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Helper dummy type or let's use String locationName
    public LostItemResponse() {
    }

    public static LostItemResponse fromEntity(LostItem item) {
        if (item == null) return null;
        LostItemResponse resp = new LostItemResponse();
        resp.setId(item.getId());
        resp.setItemName(item.getItemName());
        resp.setCategory(item.getCategory());
        resp.setDescription(item.getDescription());
        resp.setLossDateTime(item.getLossDateTime());
        resp.setTransportType(item.getTransportType());
        resp.setRouteNumber(item.getRouteNumber());
        resp.setLocation(item.getLocation());
        resp.setPhotoUrl(item.getPhotoUrl());
        if (item.getPassenger() != null) {
            resp.setPassengerId(item.getPassenger().getId());
            resp.setPassengerName(item.getPassenger().getFullName());
            resp.setPassengerPhone(item.getPassenger().getPhone());
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

    public LocalDateTime getLossDateTime() {
        return lossDateTime;
    }

    public void setLossDateTime(LocalDateTime lossDateTime) {
        this.lossDateTime = lossDateTime;
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

    public String getPassengerPhone() {
        return passengerPhone;
    }

    public void setPassengerPhone(String passengerPhone) {
        this.passengerPhone = passengerPhone;
    }

    public LostItemStatus getStatus() {
        return status;
    }

    public void setStatus(LostItemStatus status) {
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
