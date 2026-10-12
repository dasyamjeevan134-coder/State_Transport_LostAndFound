package com.transport.lostfound.entity;

import com.transport.lostfound.enums.ItemCategory;
import com.transport.lostfound.enums.LostItemStatus;
import com.transport.lostfound.enums.TransportType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lost_items", indexes = {
        @Index(name = "idx_lost_items_category", columnList = "category"),
        @Index(name = "idx_lost_items_status", columnList = "status"),
        @Index(name = "idx_lost_items_transport", columnList = "transport_type"),
        @Index(name = "idx_lost_items_passenger", columnList = "passenger_id"),
        @Index(name = "idx_lost_items_date", columnList = "loss_date_time")
})
public class LostItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name", nullable = false, length = 150)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ItemCategory category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "loss_date_time", nullable = false)
    private LocalDateTime lossDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type", nullable = false, length = 30)
    private TransportType transportType;

    @Column(name = "route_number", nullable = false, length = 50)
    private String routeNumber;

    @Column(nullable = false, length = 150)
    private String location;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LostItemStatus status = LostItemStatus.OPEN;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public LostItem() {
    }

    public LostItem(String itemName, ItemCategory category, String description,
                    LocalDateTime lossDateTime, TransportType transportType,
                    String routeNumber, String location, String photoUrl,
                    User passenger, LostItemStatus status) {
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.lossDateTime = lossDateTime;
        this.transportType = transportType;
        this.routeNumber = routeNumber;
        this.location = location;
        this.photoUrl = photoUrl;
        this.passenger = passenger;
        this.status = status != null ? status : LostItemStatus.OPEN;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = LostItemStatus.OPEN;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

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

    public User getPassenger() {
        return passenger;
    }

    public void setPassenger(User passenger) {
        this.passenger = passenger;
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
