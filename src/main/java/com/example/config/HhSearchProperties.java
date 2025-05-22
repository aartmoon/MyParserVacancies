package com.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hh.search")
public class HhSearchProperties {

    private String keyword;
    private String searchField;
    private int area;
    private int minSalary;
    private String currency;
    private boolean onlyWithSalary;
    private int perPage;

    public String getKeyword() {
        return keyword;
    }
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getSearchField() {
        return searchField;
    }
    public void setSearchField(String searchField) {
        this.searchField = searchField;
    }

    public int getArea() { return area; }
    public void setArea(int area) { this.area = area; }

    public int getMinSalary() { return minSalary; }
    public void setMinSalary(int minSalary) { this.minSalary = minSalary; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public boolean isOnlyWithSalary() { return onlyWithSalary; }
    public void setOnlyWithSalary(boolean onlyWithSalary) {
        this.onlyWithSalary = onlyWithSalary;
    }

    public int getPerPage() { return perPage; }
    public void setPerPage(int perPage) { this.perPage = perPage; }
}