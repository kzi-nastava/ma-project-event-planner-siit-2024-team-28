package com.eventplanner.adapters.typeAdapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.format(formatter));
        }
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
        // Handle JSON nulls gracefully
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String str = in.nextString();
        if (str == null || str.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(str, formatter);
    }
}
