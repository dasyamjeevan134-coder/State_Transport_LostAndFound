package com.transport.lostfound.dto.request;

import com.transport.lostfound.enums.ItemCategory;
import com.transport.lostfound.enums.TransportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class FoundItemCreateRequest {

    @NotBlank(message = "Item name is required")
    @Size(min = 2, max = 150, message = "Item name must be between 2 and 150 characters")
    private String itemName;

    @NotNull(message = "Category is required")
    private ItemCategory category;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    @NotNull(message = "Found date and time is required")
    @PastOrPresent(message = "Found date and time cannot be in the future")
    private LocalDateTime foundDateTime;

    @NotNull(message = "Transport type is required")
    private TransportType transportType;

    @NotBlank(message = "Route number is required")
    @Size(max = 50, message = "Route number cannot exceed 50 characters")
    private String routeNumber;

    @NotBlank(message = "Location is required")
    @Size(max = 150, message = "Location cannot exceed 150 characters")
    private String location;

    @Size(max = 500, message = "Photo URL cannot exceed 500 characters")
    private String photoUrl;

    public FoundItemCreateRequest() {
    }

    public FoundItemCreateRequest(String itemName, ItemCategory category, String description,
                                  LocalDateTime foundDateTime, TransportType transportType,
                                  String routeNumber, String location, String photoUrl) {
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.foundDateTime = foundDateTime;
        this.transportType = transportType;
        this.routeNumber = routeNumber;
        this.location = location;
        this.photoUrl = photoUrl;
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
}
