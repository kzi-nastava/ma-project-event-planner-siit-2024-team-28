package com.eventplanner.model.solutions;

import com.eventplanner.R;

public class Service extends Product{
    private String specifics;
    private int durationInSeconds;
    private int reservationDeadlineDays;
    private int cancellationDeadlineDays;
    private ReservationType reservationType;
    public Service(String name, String description, double price, double discount, String specifics, int durationInSeconds,
                   int reservationDeadlineDays, int cancellationDeadlineDays, ReservationType reservationType)
    {
        super(name, description, price, discount);
        this.image = R.drawable.ketering;
        this.specifics = specifics;
        this.durationInSeconds = durationInSeconds;
        this.reservationDeadlineDays = reservationDeadlineDays;
        this.cancellationDeadlineDays = cancellationDeadlineDays;
        this.reservationType = reservationType;
    }

    public String getSpecifics() {
        return specifics;
    }

    public int getDurationInSeconds() {
        return durationInSeconds;
    }

    public int getReservationDeadlineDays() {
        return reservationDeadlineDays;
    }

    public int getCancellationDeadlineDays() {
        return cancellationDeadlineDays;
    }

    public ReservationType getReservationType() {
        return reservationType;
    }
}
