package com.todoapp.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryDTO {

  private Long id;

  @NotBlank(message = "Category name is required")
  @Size(max = 100, message = "Category name must not exceed 100 characters")
  private String name;

  @Size(max = 7, message = "Color must be a valid hex code (#RRGGBB)")
  private String color;

  private String createdAt;
  private String updatedAt;

  public CategoryDTO() {}

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
