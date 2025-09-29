package com.eventplanner.model.enums;

import androidx.annotation.NonNull;

public enum PrivacyType {
    PUBLIC, PRIVATE;

    @NonNull
    @Override
    public String toString() {
        switch (this) {
            case PUBLIC: return "Public";
            case PRIVATE: return "Private";
            default: return super.toString();
        }
    }
}
