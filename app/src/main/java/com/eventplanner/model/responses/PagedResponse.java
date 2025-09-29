package com.eventplanner.model.responses;

import java.util.List;

public class PagedResponse<T> {
    private List<T> content;
    private int totalPages;
    private long totalElements;
    private int number;
    private int size;
    private boolean last;
    private boolean first;

    public List<T> getContent() {
        return content;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getNumber() {
        return number;
    }

    public int getSize() {
        return size;
    }

    public boolean isLast() {
        return last;
    }

    public boolean isFirst() {
        return first;
    }
}
