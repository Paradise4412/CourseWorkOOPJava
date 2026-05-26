import persistence.DatabaseConnectionManager;
import presentation.ConsoleApplication;

public class DetentionRegistryApplication {

    public static void main(String[] args) {
        ConsoleApplication application = new ConsoleApplication();
        try {
            application.run();
        } finally {
            DatabaseConnectionManager.close();
        }
    }
}
