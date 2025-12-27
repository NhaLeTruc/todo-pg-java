package com.todoapp.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TagDTO {

  private Long id;

  @NotBlank(message = "Tag name is required")
  @Size(max = 50, message = "Tag name must not exceed 50 characters")
  private String name;

  @Size(max = 7, message = "Color must be a valid hex code (#RRGGBB)")
  private String color;

  private String createdAt;
  private String updatedAt;

  public TagDTO() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
  }
}
