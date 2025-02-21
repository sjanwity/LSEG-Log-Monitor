import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ConsoleLogger {
    private static final Logger logger = Logger.getLogger(ConsoleLogger.class.getName());

    // Static list to store log records for testing
    private static final List<LogRecord> logRecords = new ArrayList<>();

    //no need to worry about passing class names just yet
    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter() {
            //override formatter so it doesn't print the current timestamp and differentiates the colors
            @Override
            public String format(LogRecord record) {
                String color;
                String level = record.getLevel().getName();
                color = switch (level) {
                    case "SEVERE" -> "\u001B[31m";
                    case "WARNING" -> "\u001B[33m";
                    case "INFO" -> "\u001B[32m";
                    default -> "\u001B[32m";
                };
                // Store the record for testing
                logRecords.add(record);
                return color + "[" + level + "] " + record.getMessage() + "\n";
            }
        });
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
    }
    public static Logger getNewLogger() {
        return logger;
    }

    public static void clearLogRecords() {
        logRecords.clear();
    }

    public static boolean hasLogWithLevelAndMessage(String level, String messageContent) {
        return logRecords.stream()
                .anyMatch(r -> r.getLevel().getName().equals(level) &&
                        r.getMessage().contains(messageContent));
    }

    public static long countLogsWithLevel(String level) {
        return logRecords.stream()
                .filter(r -> r.getLevel().getName().equals(level))
                .count();
    }


}
