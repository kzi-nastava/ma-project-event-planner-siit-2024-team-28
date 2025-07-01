package com.eventplanner.model.requests.auth;

public class UpdateBusinessOwnerRequest {
    private String email;
    private String phoneNumber;
    private String profilePictureBase64;
    private String address;
    private String businessName;
    private String businessDescription;

    public UpdateBusinessOwnerRequest() {}

    public UpdateBusinessOwnerRequest(String email, String phoneNumber, String profilePictureBase64, String address, String businessName, String businessDescription) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.profilePictureBase64 = profilePictureBase64;
        this.address = address;
        this.businessName = businessName;
        this.businessDescription = businessDescription;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfilePictureBase64() {
        return profilePictureBase64;
    }

    public void setProfilePictureBase64(String profilePictureBase64) {
        this.profilePictureBase64 = profilePictureBase64;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessDescription() {
        return businessDescription;
    }

    public void setBusinessDescription(String businessDescription) {
        this.businessDescription = businessDescription;
    }
}

