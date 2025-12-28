package com.todoapp.domain.model;

/** Represents the frequency of a recurring task pattern. */
public enum Frequency {
  /** Task recurs daily. */
  DAILY,

  /** Task recurs weekly on specific days. */
  WEEKLY,

  /** Task recurs monthly on a specific day of the month. */
  MONTHLY
}
