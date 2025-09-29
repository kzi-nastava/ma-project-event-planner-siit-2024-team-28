package com.eventplanner.utils;

public class FormValidator {

    // Email validation (regex)
    public static boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        return email != null && email.matches(emailPattern);
    }

    // Empty or whitespace only
    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    // Minimum length check
    public static boolean hasMinLength(String text, int minLength) {
        return text != null && text.trim().length() >= minLength;
    }

    // Phone number validation (international format + basic rules)
    public static boolean isValidPhoneNumber(String phone) {
        if (isEmpty(phone)) return false;
        return phone.matches("^\\+?[0-9\\s\\-]{8,15}$");
    }

    // Address validation (min length + allowed chars)
    public static boolean isValidAddress(String address) {
        if (!hasMinLength(address, 5)) return false;
        // Allowed chars: letters, digits, space, comma, apostrophe, dash, dot, hash
        return address.matches("^[a-zA-Z0-9\\s,'\\-.#]{5,}$");
    }

    // Password validation (min length)
    public static boolean isValidPassword(String password) {
        return hasMinLength(password, 6);
    }

    // Business name validation (min length)
    public static boolean isValidBusinessName(String name) {
        return hasMinLength(name, 3);
    }

    // Description validation (min length)
    public static boolean isValidDescription(String description) {
        return hasMinLength(description, 5);
    }
}
