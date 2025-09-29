package com.eventplanner.model.solutions;

import com.eventplanner.R;

public abstract class Solution {
    protected String name;
    protected String description;
    protected int image;
    protected double price;
    protected double discount;
    protected boolean isDeleted;
    protected boolean isVisible;
    protected boolean isAvailable;
    public Solution(String name, String description, double price, double discount) {
        this.name = name;
        this.description = description;
        this.image = R.drawable.img_1;
        this.price = price;
        this.discount = discount;
        this.isDeleted = false;
        this.isVisible = false;
        this.isAvailable = false;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public int getImage()
    {
        return image;
    }

    public double getPrice() {
        return price;
    }

    public double getDiscount() {
        return discount;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public boolean isAvailable() {
        return isAvailable;
    }
}
