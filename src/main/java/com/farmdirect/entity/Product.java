package com.farmdirect.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "farmer_id")
    private User farmer;

    @NotBlank
    private String name;

    private String nameTamil;

    @NotBlank
    private String category;  // Vegetables, Fruits, Grains, Dairy, Spices

    @Positive
    private double pricePerUnit;

    @NotBlank
    private String unit;  // kg, litre, dozen, piece, bundle

    @PositiveOrZero
    private double stockQuantity;

    @Column(length = 1000)
    private String description;

    private String imageUrl;

    private LocalDate harvestDate;
    private LocalDate expiryDate;

    private boolean organic;
    private boolean available = true;

    private double rating;
    private int reviewCount;

    public Product() {}

    // ─── Freshness calculation ───

    @Transient
    public FreshnessStatus getFreshnessStatus() {
        if (expiryDate == null) return FreshnessStatus.FRESH;
        LocalDate today = LocalDate.now();
        if (today.isAfter(expiryDate)) return FreshnessStatus.EXPIRED;
        if (today.plusDays(3).isAfter(expiryDate)) return FreshnessStatus.USE_SOON;
        return FreshnessStatus.FRESH;
    }

    @Transient
    public long getDaysUntilExpiry() {
        if (expiryDate == null) return 999;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    // ─── Getters & Setters ───

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getFarmer() { return farmer; }
    public void setFarmer(User farmer) { this.farmer = farmer; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNameTamil() { return nameTamil; }
    public void setNameTamil(String nameTamil) { this.nameTamil = nameTamil; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(double pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(double stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDate getHarvestDate() { return harvestDate; }
    public void setHarvestDate(LocalDate harvestDate) { this.harvestDate = harvestDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public boolean isOrganic() { return organic; }
    public void setOrganic(boolean organic) { this.organic = organic; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
}
