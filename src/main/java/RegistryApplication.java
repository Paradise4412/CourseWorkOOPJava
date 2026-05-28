import persistence.DatabaseConnectionManager;
import presentation.ConsoleApplication;

public class RegistryApplication {

    public static void main(String[] args) {
        ConsoleApplication application = new ConsoleApplication();
        try {
            application.run();
        } finally {
            DatabaseConnectionManager.close();
        }
    }
}
