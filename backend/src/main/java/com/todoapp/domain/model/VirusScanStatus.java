package com.todoapp.domain.model;

public enum VirusScanStatus {
  PENDING, // Awaiting virus scan
  SCANNING, // Scan in progress
  CLEAN, // No viruses detected
  INFECTED, // Virus or malware detected
  SCAN_FAILED // Scan failed due to error
}
