package com.esprit.connect.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public class PaginatedResponse<T> {

    private long count;
    private String next;
    private String previous;
    private List<T> results;

    public PaginatedResponse() {}

    public PaginatedResponse(long count, String next, String previous, List<T> results) {
        this.count = count;
        this.next = next;
        this.previous = previous;
        this.results = results;
    }

    public static <T> PaginatedResponse<T> fromPage(Page<?> page, List<T> results, String basePath) {
        return fromPage(page, results);
    }

    public static <T> PaginatedResponse<T> fromPage(Page<?> page, List<T> results) {
        PaginatedResponse<T> response = new PaginatedResponse<>();
        response.setCount(page.getTotalElements());
        response.setResults(results);

        if (page.hasNext()) {
            response.setNext(String.valueOf(page.getNumber() + 2));
        } else {
            response.setNext(null);
        }

        if (page.hasPrevious()) {
            response.setPrevious(String.valueOf(page.getNumber()));
        } else {
            response.setPrevious(null);
        }

        return response;
    }

    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }

    public String getNext() { return next; }
    public void setNext(String next) { this.next = next; }

    public String getPrevious() { return previous; }
    public void setPrevious(String previous) { this.previous = previous; }

    public List<T> getResults() { return results; }
    public void setResults(List<T> results) { this.results = results; }
}
