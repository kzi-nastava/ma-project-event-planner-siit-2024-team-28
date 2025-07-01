package com.eventplanner.model.requests.auth;

public class RegisterEventOrganizerRequest {
    private String email;
    private String password;
    private String phoneNumber;
    private String profilePictureBase64; // Nullable, use `null` if no picture
    private String address;
    private String firstName;
    private String lastName;

    public RegisterEventOrganizerRequest(String email, String password, String phoneNumber, String profilePictureBase64, String address, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.profilePictureBase64 = profilePictureBase64;
        this.address = address;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
