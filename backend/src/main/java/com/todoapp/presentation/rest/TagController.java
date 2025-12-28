package com.todoapp.presentation.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.todoapp.application.dto.TagDTO;
import com.todoapp.application.service.TagService;
import com.todoapp.infrastructure.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tags")
@Tag(name = "Tags", description = "Tag management endpoints")
public class TagController {

  private final TagService tagService;

  public TagController(TagService tagService) {
    this.tagService = tagService;
  }

  @GetMapping
  @Operation(summary = "Get all tags for current user")
  public ResponseEntity<List<TagDTO>> getAllTags(@AuthenticationPrincipal UserPrincipal principal) {
    List<TagDTO> tags = tagService.getTagsByUserId(principal.getUserId());
    return ResponseEntity.ok(tags);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get tag by ID")
  public ResponseEntity<TagDTO> getTagById(
      @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
    TagDTO tag = tagService.getTagById(id, principal.getUserId());
    return ResponseEntity.ok(tag);
  }

  @PostMapping
  @Operation(summary = "Create new tag")
  public ResponseEntity<TagDTO> createTag(
      @Valid @RequestBody TagDTO tagDTO, @AuthenticationPrincipal UserPrincipal principal) {
    TagDTO createdTag = tagService.createTag(tagDTO, principal.getUserId());
    return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update tag")
  public ResponseEntity<TagDTO> updateTag(
      @PathVariable Long id,
      @Valid @RequestBody TagDTO tagDTO,
      @AuthenticationPrincipal UserPrincipal principal) {
    TagDTO updatedTag = tagService.updateTag(id, tagDTO, principal.getUserId());
    return ResponseEntity.ok(updatedTag);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete tag")
  public ResponseEntity<Void> deleteTag(
      @PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
    tagService.deleteTag(id, principal.getUserId());
    return ResponseEntity.noContent().build();
  }
}
