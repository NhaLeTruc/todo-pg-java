package com.todoapp.domain.model;

/**
 * Enum representing the type of time entry.
 *
 * <p>Time entries can be created in two ways:
 *
 * <ul>
 *   <li>TIMER - Created by starting and stopping a timer
 *   <li>MANUAL - Manually logged time by the user
 * </ul>
 */
public enum EntryType {
  /** Time entry created using a start/stop timer */
  TIMER,

  /** Time entry manually logged by the user */
  MANUAL
}
