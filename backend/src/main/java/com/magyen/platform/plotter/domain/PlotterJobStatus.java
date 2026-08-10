package com.magyen.platform.plotter.domain;

/**
 * Estado operacional de un trabajo de plotter.
 */
public enum PlotterJobStatus {
    REGISTERED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
