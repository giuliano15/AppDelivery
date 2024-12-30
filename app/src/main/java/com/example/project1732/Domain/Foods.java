//package com.example.project1732.Domain;
package com.example.project1732.Domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Foods implements Serializable {
    private String firebaseOrderId;
    private int CategoryId;
    private String Description;
    private boolean BestFood;
    private int Id;
    private int LocationId;
    private double Price;
    private String ImagePath;
    private int PriceId;
    private double Star;
    private int TimeId;
    private int TimeValue;
    private String Title;
    private int numberInCart;

    // Construtor padrão
    public Foods() {
    }

    public String getFirebaseOrderId() {
        return firebaseOrderId;
    }

    public void setFirebaseOrderId(String firebaseOrderId) {
        this.firebaseOrderId = firebaseOrderId;
    }

    // Getters e setters
    public int getCategoryId() {
        return CategoryId;
    }

    public void setCategoryId(int categoryId) {
        CategoryId = categoryId;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public boolean isBestFood() {
        return BestFood;
    }

    public void setBestFood(boolean bestFood) {
        BestFood = bestFood;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getLocationId() {
        return LocationId;
    }

    public void setLocationId(int locationId) {
        LocationId = locationId;
    }

    public double getPrice() {
        return Price;
    }

    public void setPrice(double price) {
        Price = price;
    }

    public String getImagePath() {
        return ImagePath;
    }

    public void setImagePath(String imagePath) {
        ImagePath = imagePath;
    }

    public int getPriceId() {
        return PriceId;
    }

    public void setPriceId(int priceId) {
        PriceId = priceId;
    }

    public double getStar() {
        return Star;
    }

    public void setStar(double star) {
        Star = star;
    }

    public int getTimeId() {
        return TimeId;
    }

    public void setTimeId(int timeId) {
        TimeId = timeId;
    }

    public int getTimeValue() {
        return TimeValue;
    }

    public void setTimeValue(int timeValue) {
        TimeValue = timeValue;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public int getNumberInCart() {
        return numberInCart;
    }

    public void setNumberInCart(int numberInCart) {
        this.numberInCart = numberInCart;
    }

    // Método para converter Foods para Map<String, Object>
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("firebaseOrderId", firebaseOrderId);
        map.put("CategoryId", CategoryId);
        map.put("Description", Description);
        map.put("BestFood", BestFood);
        map.put("Id", Id);
        map.put("LocationId", LocationId);
        map.put("Price", Price);
        map.put("ImagePath", ImagePath);
        map.put("PriceId", PriceId);
        map.put("Star", Star);
        map.put("TimeId", TimeId);
        map.put("TimeValue", TimeValue);
        map.put("Title", Title);
        map.put("numberInCart", numberInCart);
        return map;
    }
}


