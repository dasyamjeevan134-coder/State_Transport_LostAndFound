package com.transport.lostfound.entity;

import com.transport.lostfound.enums.FoundItemStatus;
import com.transport.lostfound.enums.ItemCategory;
import com.transport.lostfound.enums.TransportType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "found_items", indexes = {
        @Index(name = "idx_found_items_category", columnList = "category"),
        @Index(name = "idx_found_items_status", columnList = "status"),
        @Index(name = "idx_found_items_transport", columnList = "transport_type"),
        @Index(name = "idx_found_items_staff", columnList = "staff_id"),
        @Index(name = "idx_found_items_date", columnList = "found_date_time")
})
public class FoundItem {

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

    @Column(name = "found_date_time", nullable = false)
    private LocalDateTime foundDateTime;

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
    @JoinColumn(name = "staff_id", nullable = false)
    private User staff;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FoundItemStatus status = FoundItemStatus.AVAILABLE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public FoundItem() {
    }

    public FoundItem(String itemName, ItemCategory category, String description,
                     LocalDateTime foundDateTime, TransportType transportType,
                     String routeNumber, String location, String photoUrl,
                     User staff, FoundItemStatus status) {
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.foundDateTime = foundDateTime;
        this.transportType = transportType;
        this.routeNumber = routeNumber;
        this.location = location;
        this.photoUrl = photoUrl;
        this.staff = staff;
        this.status = status != null ? status : FoundItemStatus.AVAILABLE;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = FoundItemStatus.AVAILABLE;
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

    public User getStaff() {
        return staff;
    }

    public void setStaff(User staff) {
        this.staff = staff;
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
