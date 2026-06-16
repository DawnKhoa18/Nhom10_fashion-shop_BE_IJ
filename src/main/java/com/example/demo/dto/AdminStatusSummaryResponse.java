package com.example.demo.dto;

public class AdminStatusSummaryResponse {
    private String label;
    private Long count;

    public AdminStatusSummaryResponse(String label, Long count) {
        this.label = label;
        this.count = count;
    }

    public String getLabel() { return label; }
    public Long getCount() { return count; }
}
