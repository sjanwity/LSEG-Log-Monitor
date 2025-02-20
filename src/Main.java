import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        String logFilePath = "resources/logs.log";
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter() {
            //override formatter so it doesn't print the current timestamp abd differentiates the colors
            @Override
            public String format(LogRecord record) {
                String color;
                String level = record.getLevel().getName();
                switch (level) {
                    case "SEVERE":
                        color =  "\u001B[31m";
                        break;
                    case "WARNING":
                        color = "\u001B[33m";
                        break;
                    case "INFO":
                        color = "\u001B[32m";
                        break;
                    default: color = "\u001B[32m";
                }
                return color + "[" + level + "] " + record.getMessage() + "\n";
            }
        });
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
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