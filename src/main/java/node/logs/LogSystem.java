package node.logs;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogSystem {
    private static Logger logger = null;

    private static void startLogSystem() {
        try {
            logger = Logger.getLogger("nodeLogs");
            FileHandler fhandler = new FileHandler("logs/nodeLogs.log");
            logger.addHandler(fhandler);
            SimpleFormatter formatter = new SimpleFormatter();
            fhandler.setFormatter(formatter);
        } catch (SecurityException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    
    public static void logInfoMessage(String message) {
        if (logger == null)
            startLogSystem();
        logger.log(Level.INFO, message);
    }

    public static void logErrorMessage(String message) {
        if (logger == null)
            startLogSystem();
        logger.log(Level.WARNING, message);
    }

}
