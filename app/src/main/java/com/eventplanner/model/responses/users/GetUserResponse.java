package com.eventplanner.model.responses.users;

public class GetUserResponse {
    private Long id;
    private String email;
    private String phoneNumber;
    private String address;
    private Boolean isActive;
    private Boolean isDeleted;
    private String businessName;
    private String businessDescription;
    private String firstName;
    private String lastName;

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getBusinessDescription() {
        return businessDescription;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
