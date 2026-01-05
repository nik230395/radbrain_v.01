package org.nikolic.programm.dtos;

public class CreateContentRequest {
    private String contentType;
    private String title;
    private String contentData;
    private Integer displayOrder;
    private String metadata;

    // Getters and Setters
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContentData() { return contentData; }
    public void setContentData(String contentData) { this.contentData = contentData; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}