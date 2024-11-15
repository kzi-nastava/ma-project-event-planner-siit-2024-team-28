package com.eventplanner.model.solutions;

import com.eventplanner.R;

public class Solution {
    private String name;
    private String description;
    private int image;
    public Solution(String name, String description) {
        this.name = name;
        this.description = description;
        this.image = R.drawable.img_1;
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
}
