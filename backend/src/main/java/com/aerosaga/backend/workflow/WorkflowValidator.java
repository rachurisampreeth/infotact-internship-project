package com.aerosaga.backend.workflow;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validates workflow-related input for the AeroSaga backend.
 */
public final class WorkflowValidator {

    private static final Set<String> VALID_STATUSES =
            new HashSet<>(Arrays.asList(
                    "PENDING", "IN_PROGRESS", "COMPLETED",
                    "FAILED", "CANCELLED"
            ));

    private static final Set<String> VALID_PRIORITIES =
            new HashSet<>(Arrays.asList(
                    "LOW", "MEDIUM", "HIGH", "CRITICAL"
            ));

    private static final Pattern SAFE_IDENTIFIER =
            Pattern.compile("^[a-zA-Z0-9_-]+$");

    private WorkflowValidator() {
        throw new UnsupportedOperationException(
                "Utility class cannot be instantiated"
        );
    }

    public static boolean isValidName(String name) {
        return name != null
                && !name.trim().isEmpty()
                && name.trim().length() <= 100;
    }

    public static boolean isValidDescription(String description) {
        return description != null
                && description.trim().length() <= 1000;
    }

    public static boolean isValidIdentifier(String identifier) {
        return identifier != null
                && !identifier.isEmpty()
                && identifier.length() <= 64
                && SAFE_IDENTIFIER.matcher(identifier).matches();
    }

    public static String normalizeStatus(String status) {
        if (status == null) {
            return "";
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    public static boolean isValidStatus(String status) {
        return VALID_STATUSES.contains(normalizeStatus(status));
    }

    public static String normalizePriority(String priority) {
        if (priority == null) {
            return "";
        }
        return priority.trim().toUpperCase(Locale.ROOT);
    }

    public static boolean isValidPriority(String priority) {
        return VALID_PRIORITIES.contains(normalizePriority(priority));
    }

    public static boolean isTerminalStatus(String status) {
        String normalized = normalizeStatus(status);
        return "COMPLETED".equals(normalized)
                || "FAILED".equals(normalized)
                || "CANCELLED".equals(normalized);
    }

    public static boolean canTransition(
            String currentStatus, String nextStatus) {

        String current = normalizeStatus(currentStatus);
        String next = normalizeStatus(nextStatus);

        if (!isValidStatus(current) || !isValidStatus(next)) {
            return false;
        }

        if (current.equals(next)) {
            return true;
        }

        if (isTerminalStatus(current)) {
            return false;
        }

        switch (current) {
            case "PENDING":
                return "IN_PROGRESS".equals(next)
                        || "CANCELLED".equals(next);

            case "IN_PROGRESS":
                return "COMPLETED".equals(next)
                        || "FAILED".equals(next)
                        || "CANCELLED".equals(next);

            default:
                return false;
        }
    }

    public static boolean isValidRetryCount(int retryCount) {
        return retryCount >= 0 && retryCount <= 5;
    }

    public static boolean isValidTimeout(long timeoutMillis) {
        return timeoutMillis >= 100
                && timeoutMillis <= 300000;
    }

    public static boolean isValidAttemptNumber(int attempt) {
        return attempt >= 1 && attempt <= 10;
    }

    public static String sanitizeName(String name) {
        if (name == null) {
            return "";
        }

        String sanitized = name.trim()
                .replaceAll("\\s+", " ");

        return sanitized.length() > 100
                ? sanitized.substring(0, 100)
                : sanitized;
    }

    public static boolean isValidProgress(int progress) {
        return progress >= 0 && progress <= 100;
    }

    public static boolean isValidTimestamp(long timestamp) {
        return timestamp > 0;
    }

    public static boolean isValidErrorMessage(String message) {
        return message != null && message.length() <= 500;
    }

    public static boolean isValidWorkflowInput(
            String name, String status, String priority) {

        return isValidName(name)
                && isValidStatus(status)
                && isValidPriority(priority);
    }

    public static String getValidationError(
            String name, String status, String priority) {

        if (!isValidName(name)) {
            return "Workflow name must contain 1-100 characters.";
        }

        if (!isValidStatus(status)) {
            return "Workflow status is invalid.";
        }

        if (!isValidPriority(priority)) {
            return "Workflow priority is invalid.";
        }

        return "";
    }
}
