package com.example.mrservice.models;

import java.io.Serializable;

public class TaskCat implements Serializable {

    private String categoryId, categoryName, categoryImageUrl;

    public TaskCat() {
    }

    public TaskCat(String categoryId, String categoryName, String categoryImageUrl) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryImageUrl = categoryImageUrl;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getCategoryImageUrl() {
        return categoryImageUrl;
    }
}
