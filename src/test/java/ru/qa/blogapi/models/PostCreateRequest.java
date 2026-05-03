package ru.qa.blogapi.models;

public class PostCreateRequest {
    private String title;
    private String body;
    private String description;
    private String category;
    private Boolean isDraft;

    public PostCreateRequest() {
    }

    public PostCreateRequest(String title, String body, String description, String category, Boolean isDraft) {
        this.title = title;
        this.body = body;
        this.description = description;
        this.category = category;
        this.isDraft = isDraft;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public Boolean getIsDraft() {
        return isDraft;
    }
}
