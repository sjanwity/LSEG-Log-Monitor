import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class ProcessLogs {
    // Constants for threshold durations
    private final Duration WARNING_THRESHOLD;
    private final Duration ERROR_THRESHOLD;
    private final Logger logger;

    // Map to store job start times, with job ID as key
    private final Map<String, JobStatus> jobMap;

    public static class Builder {
        //Using Builder pattern to build the ProcessLogs object
        private Duration warningThreshold;
        private Duration errorThreshold;
        private Logger logger;

        public Builder warningThreshold(Duration threshold) {
            this.warningThreshold = threshold;
            return this;
        }

        public Builder errorThreshold(Duration threshold) {
            this.errorThreshold = threshold;
            return this;
        }

        public Builder logger(Logger logger)
        {
            this.logger = logger;
            return this;
        }

        public ProcessLogs build() {
            validateThresholds();
            return new ProcessLogs(this);
        }

        //some builder property validations
        private void validateThresholds() {
            if (warningThreshold.isNegative() || warningThreshold.isZero()) {
                throw new IllegalArgumentException("Warning threshold must be positive");
            }
            if (errorThreshold.isNegative() || errorThreshold.isZero()) {
                throw new IllegalArgumentException("Error threshold must be positive");
            }
            if (errorThreshold.compareTo(warningThreshold) <= 0) {
                throw new IllegalArgumentException("Error threshold must be greater than warning threshold");
            }
        }
    }

    private ProcessLogs(Builder builder) {
        this.WARNING_THRESHOLD = builder.warningThreshold;
        this.ERROR_THRESHOLD = builder.errorThreshold;
        this.jobMap = new HashMap<>();
        this.logger = builder.logger;

    }

    public void processLogFile(Path filePath) throws IOException {
        try (Stream<String> lines = Files.lines(filePath)) {
            //process the file given on main, line by line
            lines.forEach(this::processLogEntry);
        } catch (IOException ioe)
        {
            logger.severe("Invalid log file specified!");
        }
        // Report any jobs that haven't finished
        for (Map.Entry<String, JobStatus> entry : jobMap.entrySet()) {
            if (entry.getValue().getEndTime() == null) {
                logger.severe("Job " + entry.getKey() + " started but never finished");
            }
        }
    }

    void processLogEntry(String logEntry) {
        String[] parts = logEntry.split(",");
        if (parts.length < 4) {
            logger.warning("[SKIPPING] Invalid log entry format: " + logEntry);
            return;
        }

        // Parse timestamp
        LocalTime time;
        try {
            time = LocalTime.parse(parts[0].trim(), DateTimeFormatter.ofPattern("HH:mm:ss"));

        } catch (Exception e) {
            logger.warning("[SKIPPING] Invalid log entry format: " + logEntry);
            return;
        }

        String description = parts[1];
        String status = parts[2];
        String pid = parts[3];

        // Create a unique job ID combining the description and PID
        String jobId = description.trim() + "-" + pid.trim();


        if ("START".equals(status.trim())) {
            // Record job start
            jobMap.put(jobId, new JobStatus(time, null, description));
        } else if ("END".equals(status.trim())) {
            // Process job end
            JobStatus job = jobMap.get(jobId);
            if (job == null) {
                logger.severe("Found END without START for job: " + jobId);
                return;
            }

            job.setEndTime(time);

            // Calculate duration
            Duration duration = calculateDuration(job.getStartTime(), job.getEndTime());

            // Check against thresholds and report
            if (duration.compareTo(ERROR_THRESHOLD) > 0) {
                logger.severe(jobId + " took " + formatDuration(duration) +
                        " (started at " + job.getStartTime() + ", ended at " + job.getEndTime() + ")");
            } else if (duration.compareTo(WARNING_THRESHOLD) > 0) {
                logger.warning(jobId + " took " + formatDuration(duration) +
                        " (started at " + job.getStartTime() + ", ended at " + job.getEndTime() + ")");
            } else {
                logger.info(jobId + " completed in " + formatDuration(duration));
            }
        }
    }
    private Duration calculateDuration(LocalTime start, LocalTime end) {
        // Handle case where job runs past midnight
        if (end.isBefore(start)) {
            // Job ran past midnight: calculate as if it ran into the next day
            return Duration.between(start, end).plusHours(24);
        }
        return Duration.between(start, end);
    }

    private String formatDuration(Duration duration) {
        long totalSeconds = duration.getSeconds();
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return minutes + " minutes, " + seconds + " seconds.";
    }
}
