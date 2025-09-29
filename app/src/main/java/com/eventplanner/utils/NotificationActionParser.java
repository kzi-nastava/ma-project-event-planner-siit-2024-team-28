package com.eventplanner.utils;

public class NotificationActionParser {

    public static class ParsedAction {
        public final String route;
        public final String id; // can be null if route has no id

        ParsedAction(String route, String id) {
            this.route = route;
            this.id = id;
        }
    }

    public static ParsedAction parse(String actionUrl) {
        if (actionUrl == null || actionUrl.isEmpty()) {
            return new ParsedAction("", null);
        }

        // remove leading slash if present
        String cleanUrl = actionUrl.startsWith("/") ? actionUrl.substring(1) : actionUrl;

        String[] parts = cleanUrl.split("/");
        String route = parts[0];
        String id = parts.length > 1 ? parts[1] : null;

        return new ParsedAction(route, id);
    }
}