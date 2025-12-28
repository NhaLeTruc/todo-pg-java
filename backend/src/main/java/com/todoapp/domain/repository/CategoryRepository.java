package com.todoapp.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.todoapp.domain.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  List<Category> findByUserId(Long userId);

  Optional<Category> findByIdAndUserId(Long id, Long userId);

  boolean existsByNameAndUserId(String name, Long userId);

  boolean existsByNameAndUserIdAndIdNot(String name, Long userId, Long id);

  void deleteByIdAndUserId(Long id, Long userId);
}
