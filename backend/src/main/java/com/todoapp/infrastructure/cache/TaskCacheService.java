package com.todoapp.infrastructure.cache;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.todoapp.domain.model.Task;

/**
 * Redis caching service for Task entities. Caches frequently accessed tasks to reduce database load
 * and improve response times.
 */
@Service
public class TaskCacheService {

  private static final Logger logger = LoggerFactory.getLogger(TaskCacheService.class);

  private static final String TASK_CACHE_NAME = "tasks";
  private static final String USER_TASKS_CACHE_NAME = "userTasks";
  private static final Duration CACHE_TTL = Duration.ofMinutes(15);

  private final RedisTemplate<String, Object> redisTemplate;

  public TaskCacheService(RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  /**
   * Cache a single task by ID.
   *
   * @param task Task to cache
   * @return Cached task
   */
  @CachePut(value = TASK_CACHE_NAME, key = "#task.id")
  public Task cacheTask(Task task) {
    logger.debug("Caching task with ID: {}", task.getId());
    return task;
  }

  /**
   * Retrieve a task from cache by ID.
   *
   * @param taskId Task ID
   * @return Cached task if present
   */
  @Cacheable(value = TASK_CACHE_NAME, key = "#taskId")
  public Optional<Task> getCachedTask(Long taskId) {
    logger.debug("Retrieving task from cache with ID: {}", taskId);
    return Optional.empty(); // Spring will populate from cache if available
  }

  /**
   * Evict a task from cache by ID.
   *
   * @param taskId Task ID to evict
   */
  @CacheEvict(value = TASK_CACHE_NAME, key = "#taskId")
  public void evictTask(Long taskId) {
    logger.debug("Evicting task from cache with ID: {}", taskId);
  }

  /**
   * Evict all tasks for a user from cache.
   *
   * @param userId User ID
   */
  @CacheEvict(value = USER_TASKS_CACHE_NAME, key = "#userId")
  public void evictUserTasks(Long userId) {
    logger.debug("Evicting all tasks from cache for user ID: {}", userId);
  }

  /**
   * Cache user tasks list.
   *
   * @param userId User ID
   * @param tasks List of tasks
   */
  @CachePut(value = USER_TASKS_CACHE_NAME, key = "#userId")
  public List<Task> cacheUserTasks(Long userId, List<Task> tasks) {
    logger.debug("Caching {} tasks for user ID: {}", tasks.size(), userId);
    return tasks;
  }

  /**
   * Get cached user tasks list.
   *
   * @param userId User ID
   * @return Cached tasks for user if present
   */
  @Cacheable(value = USER_TASKS_CACHE_NAME, key = "#userId")
  public Optional<List<Task>> getCachedUserTasks(Long userId) {
    logger.debug("Retrieving tasks from cache for user ID: {}", userId);
    return Optional.empty(); // Spring will populate from cache if available
  }

  /** Clear all task caches. */
  @CacheEvict(
      value = {TASK_CACHE_NAME, USER_TASKS_CACHE_NAME},
      allEntries = true)
  public void clearAllCaches() {
    logger.info("Clearing all task caches");
  }

  /**
   * Manually cache a task with custom TTL.
   *
   * @param task Task to cache
   * @param ttl Time to live
   */
  public void cacheTaskWithTTL(Task task, Duration ttl) {
    String key = TASK_CACHE_NAME + "::" + task.getId();
    redisTemplate.opsForValue().set(key, task, ttl);
    logger.debug("Manually cached task {} with TTL: {}", task.getId(), ttl);
  }

  /**
   * Get cache statistics for monitoring.
   *
   * @return Cache stats
   */
  public CacheStats getCacheStats() {
    // This is a simplified implementation
    // In production, use Redis INFO command or Micrometer metrics
    return new CacheStats(
        redisTemplate.keys(TASK_CACHE_NAME + "::*").size(),
        redisTemplate.keys(USER_TASKS_CACHE_NAME + "::*").size());
  }

  /** Cache statistics holder. */
  public static class CacheStats {
    private final long taskCacheSize;
    private final long userTasksCacheSize;

    public CacheStats(long taskCacheSize, long userTasksCacheSize) {
      this.taskCacheSize = taskCacheSize;
      this.userTasksCacheSize = userTasksCacheSize;
    }

    public long getTaskCacheSize() {
      return taskCacheSize;
    }

    public long getUserTasksCacheSize() {
      return userTasksCacheSize;
    }
  }
}
