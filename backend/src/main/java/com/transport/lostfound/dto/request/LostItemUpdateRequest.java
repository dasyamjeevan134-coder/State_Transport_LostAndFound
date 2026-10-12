package com.transport.lostfound.dto.request;

import com.transport.lostfound.enums.ItemCategory;
import com.transport.lostfound.enums.LostItemStatus;
import com.transport.lostfound.enums.TransportType;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class LostItemUpdateRequest {

    @Size(min = 2, max = 150, message = "Item name must be between 2 and 150 characters")
    private String itemName;

    private ItemCategory category;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    private LocalDateTime lossDateTime;

    private TransportType transportType;

    @Size(max = 50, message = "Route number cannot exceed 50 characters")
    private String routeNumber;

    @Size(max = 150, message = "Location cannot exceed 150 characters")
    private String location;

    @Size(max = 500, message = "Photo URL cannot exceed 500 characters")
    private String photoUrl;

    private LostItemStatus status;

    public LostItemUpdateRequest() {
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

    public LostItemStatus getStatus() {
        return status;
    }

    public void setStatus(LostItemStatus status) {
        this.status = status;
    }
}
