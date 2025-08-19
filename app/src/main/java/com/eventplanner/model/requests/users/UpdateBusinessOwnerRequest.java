package com.eventplanner.model.requests.users;

public class UpdateBusinessOwnerRequest {
    private String phoneNumber;
    private String profilePictureBase64;
    private String address;
    private String businessDescription;

    public UpdateBusinessOwnerRequest() {}

    public UpdateBusinessOwnerRequest(String phoneNumber, String profilePictureBase64, String address, String businessDescription) {
        this.phoneNumber = phoneNumber;
        this.profilePictureBase64 = profilePictureBase64;
        this.address = address;
        this.businessDescription = businessDescription;
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

    public String getBusinessDescription() {
        return businessDescription;
    }

    public void setBusinessDescription(String businessDescription) {
        this.businessDescription = businessDescription;
    }
}

