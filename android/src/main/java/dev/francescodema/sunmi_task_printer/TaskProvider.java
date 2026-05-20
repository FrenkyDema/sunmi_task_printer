package dev.francescodema.sunmi_task_printer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Provides dedicated asynchronous threading resources for hardware operations.
 */
public class TaskProvider {
    /**
     * Single worker pool ensuring sequential, first-in-first-out execution
     * of physical printer commands to prevent overlapping hardware failures.
     */
    public static final ExecutorService executor = Executors.newSingleThreadExecutor();
}