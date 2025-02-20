import java.time.Duration;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ConsoleLogger {
    private static final Logger logger = Logger.getLogger(ConsoleLogger.class.getName());

    //no need to worry about passing class names just yet
    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter() {
            //override formatter so it doesn't print the current timestamp and differentiates the colors
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
    }
    public static Logger getNewLogger() {
        return logger;
    }


}
