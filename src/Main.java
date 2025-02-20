import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {


    public static void main(String[] args) {
        String logFilePath = "resources/logs.log";
        Logger logger = ConsoleLogger.getNewLogger();

        ProcessLogs processLogs = new ProcessLogs.Builder()
                .warningThreshold(Duration.ofMinutes(5))
                .errorThreshold(Duration.ofMinutes(10))
                .logger(logger)
                .build();
        try {
            processLogs.processLogFile(Paths.get(logFilePath));
        } catch (IOException e) {
            logger.severe("Error reading log file: " + e.getMessage());
            e.printStackTrace();
        }
    }


}