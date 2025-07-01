package com.eventplanner.model.responses.users;

public class GetUserProfilePictureResponse {
    private String profilePictureBase64;

    public GetUserProfilePictureResponse() {
    }

    public GetUserProfilePictureResponse(String profilePictureBase64) {
        this.profilePictureBase64 = profilePictureBase64;
    }

    public String getProfilePictureBase64() {
        return profilePictureBase64;
    }

    public void setProfilePictureBase64(String profilePictureBase64) {
        this.profilePictureBase64 = profilePictureBase64;
    }
}
