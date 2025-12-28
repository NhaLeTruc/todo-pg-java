package com.todoapp.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.todoapp.domain.model.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

  List<Tag> findByUserId(Long userId);

  Optional<Tag> findByIdAndUserId(Long id, Long userId);

  boolean existsByNameAndUserId(String name, Long userId);

  boolean existsByNameAndUserIdAndIdNot(String name, Long userId, Long id);

  void deleteByIdAndUserId(Long id, Long userId);

  List<Tag> findByIdInAndUserId(List<Long> ids, Long userId);
}
