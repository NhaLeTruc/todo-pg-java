package com.todoapp.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.todoapp.application.dto.TagDTO;
import com.todoapp.domain.model.Tag;
import com.todoapp.domain.model.User;
import com.todoapp.domain.repository.TagRepository;
import com.todoapp.domain.repository.UserRepository;

@Service
public class TagService {

  private final TagRepository tagRepository;
  private final UserRepository userRepository;

  public TagService(TagRepository tagRepository, UserRepository userRepository) {
    this.tagRepository = tagRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public TagDTO createTag(TagDTO tagDTO, Long userId) {
    User user =
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

    if (tagRepository.existsByNameAndUserId(tagDTO.getName(), userId)) {
      throw new RuntimeException("Tag with name '" + tagDTO.getName() + "' already exists");
    }

    Tag tag = new Tag();
    tag.setName(tagDTO.getName());
    tag.setColor(tagDTO.getColor());
    tag.setUser(user);

    Tag savedTag = tagRepository.save(tag);
    return mapToDTO(savedTag);
  }

  @Transactional(readOnly = true)
  public List<TagDTO> getTagsByUserId(Long userId) {
    return tagRepository.findByUserId(userId).stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public TagDTO getTagById(Long id, Long userId) {
    Tag tag =
        tagRepository
            .findByIdAndUserId(id, userId)
            .orElseThrow(() -> new RuntimeException("Tag not found"));
    return mapToDTO(tag);
  }

  @Transactional
  public TagDTO updateTag(Long id, TagDTO tagDTO, Long userId) {
    Tag tag =
        tagRepository
            .findByIdAndUserId(id, userId)
            .orElseThrow(() -> new RuntimeException("Tag not found"));

    if (tagRepository.existsByNameAndUserIdAndIdNot(tagDTO.getName(), userId, id)) {
      throw new RuntimeException("Tag with name '" + tagDTO.getName() + "' already exists");
    }

    tag.setName(tagDTO.getName());
    tag.setColor(tagDTO.getColor());

    Tag updatedTag = tagRepository.save(tag);
    return mapToDTO(updatedTag);
  }

  @Transactional
  public void deleteTag(Long id, Long userId) {
    Tag tag =
        tagRepository
            .findByIdAndUserId(id, userId)
            .orElseThrow(() -> new RuntimeException("Tag not found"));

    tagRepository.delete(tag);
  }

  private TagDTO mapToDTO(Tag tag) {
    TagDTO dto = new TagDTO();
    dto.setId(tag.getId());
    dto.setName(tag.getName());
    dto.setColor(tag.getColor());
    dto.setCreatedAt(tag.getCreatedAt() != null ? tag.getCreatedAt().toString() : null);
    dto.setUpdatedAt(tag.getUpdatedAt() != null ? tag.getUpdatedAt().toString() : null);
    return dto;
  }
}
