package com.farmdirect.entity;

import jakarta.persistence.*;

@Entity
public class FarmerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private String farmName;
    private double farmSizeAcres;
    private String cropsGrown;        // comma-separated
    private String farmingType;       // Organic, Traditional, Hydroponic
    private int yearsExperience;
    private double rating;
    private int totalOrders;
    private String description;
    private boolean offersDelivery = true;
    private boolean offersPickup = true;

    public FarmerProfile() {}

    // ─── Getters & Setters ───

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }

    public double getFarmSizeAcres() { return farmSizeAcres; }
    public void setFarmSizeAcres(double farmSizeAcres) { this.farmSizeAcres = farmSizeAcres; }

    public String getCropsGrown() { return cropsGrown; }
    public void setCropsGrown(String cropsGrown) { this.cropsGrown = cropsGrown; }

    public String getFarmingType() { return farmingType; }
    public void setFarmingType(String farmingType) { this.farmingType = farmingType; }

    public int getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(int yearsExperience) { this.yearsExperience = yearsExperience; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isOffersDelivery() { return offersDelivery; }
    public void setOffersDelivery(boolean offersDelivery) { this.offersDelivery = offersDelivery; }

    public boolean isOffersPickup() { return offersPickup; }
    public void setOffersPickup(boolean offersPickup) { this.offersPickup = offersPickup; }
}
