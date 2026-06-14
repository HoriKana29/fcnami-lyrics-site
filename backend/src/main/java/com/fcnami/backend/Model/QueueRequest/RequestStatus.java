package com.fcnami.backend.Model.QueueRequest;

/**
 * Enumeration of request states in the queue system.
 * States include WAITING, IN_PROGRESS, DONE, and REJECTED.
 */
public enum RequestStatus {
    WAITING,
    IN_PROGRESS,
    DONE,
    REJECTED
}
