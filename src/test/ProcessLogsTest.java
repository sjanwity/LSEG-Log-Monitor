
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class ProcessLogsTest {
    private ProcessLogs processLogs;
    private Logger logger;

    @BeforeEach
    void setUp() {
        ConsoleLogger.clearLogRecords(); // Clear previous logs
        logger = ConsoleLogger.getNewLogger();
        processLogs = new ProcessLogs.Builder()
                .warningThreshold(Duration.ofMinutes(5))
                .errorThreshold(Duration.ofMinutes(10))
                .logger(logger)
                .build();
    }

    @Test
    void testNormalJobExecution() {
        processLogEntry("19:00:00, BackupJob, START, 12345");
        processLogEntry("19:02:00, BackupJob, END, 12345");

        assertTrue(ConsoleLogger.hasLogWithLevelAndMessage("INFO",
                "BackupJob-12345 completed in 2 minutes, 0 seconds."));
    }

    @Test
    void testWarningThresholdExceeded() {
        processLogEntry("09:00:00, BackupJob, START, 12345");
        processLogEntry("09:06:00, BackupJob, END, 12345");

        assertTrue(ConsoleLogger.hasLogWithLevelAndMessage("WARNING",
                "BackupJob-12345 took 6 minutes"));
    }

    @Test
    void testErrorThresholdExceeded() {
        processLogEntry("09:00:00, BackupJob, START, 12345");
        processLogEntry("09:11:00, BackupJob, END, 12345");

        assertTrue(ConsoleLogger.hasLogWithLevelAndMessage("SEVERE",
                "BackupJob-12345 took 11 minutes"));
    }

    @Test
    void testMissingEndEntry() {
        //Must input a file instead
    }

    @Test
    void testMissingStartEntry() {
        processLogEntry("09:00:00, BackupJob, END, 12345");

        assertTrue(ConsoleLogger.hasLogWithLevelAndMessage("SEVERE",
                "Found END without START for job: BackupJob-12345"));
    }

    @Test
    void testInvalidLogEntries() {
        String[] invalidEntries = {
                "invalid_timestamp, BackupJob, START, 12345",
                "09:00:00, BackupJob, INVALID_STATUS, 12345",
                "09:00:00, BackupJob",
                "09:00:00"
        };

        for (String entry : invalidEntries) {
            processLogEntry(entry);

            assertTrue(ConsoleLogger.hasLogWithLevelAndMessage("WARNING",
                    "[SKIPPING] Invalid log entry format: invalid_timestamp, BackupJob, START, 12345"));
        }
    }

    @Test
    void testCrossMidnightJob() {
        processLogEntry("23:58:00, NightJob, START, 12345");
        processLogEntry("00:02:00, NightJob, END, 12345");

        assertTrue(ConsoleLogger.hasLogWithLevelAndMessage("INFO",
                "NightJob-12345 completed in 4 minutes"));
    }

    @Test
    void testMultipleSimultaneousJobs() {
        processLogEntry("09:00:00, Job1, START, 12345");
        processLogEntry("09:01:00, Job2, START, 67890");
        processLogEntry("09:02:00, Job1, END, 12345");
        processLogEntry("09:03:00, Job2, END, 67890");

        assertEquals(2, ConsoleLogger.countLogsWithLevel("INFO"));
    }

    @Test
    void testBuilderValidation() {
        assertThrows(IllegalArgumentException.class, () ->
                new ProcessLogs.Builder()
                        .warningThreshold(Duration.ofMinutes(-5))
                        .errorThreshold(Duration.ofMinutes(10))
                        .logger(logger)
                        .build()
        );

        assertThrows(IllegalArgumentException.class, () ->
                new ProcessLogs.Builder()
                        .warningThreshold(Duration.ofMinutes(10))
                        .errorThreshold(Duration.ofMinutes(5))
                        .logger(logger)
                        .build()
        );

        assertThrows(IllegalArgumentException.class, () ->
                new ProcessLogs.Builder()
                        .warningThreshold(Duration.ZERO)
                        .errorThreshold(Duration.ofMinutes(10))
                        .logger(logger)
                        .build()
        );
    }

    private void processLogEntry(String entry) {
        processLogs.processLogEntry(entry);
    }
}
