package com.eventplanner.model.events;

import com.eventplanner.R;

public class Event {
    private String name;
    private String description;
    private int image;
    public Event(String name, String description) {
        this.name = name;
        this.description = description;
        this.image = R.drawable.img;
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
