package ru.istu.police;

import ru.istu.police.db.Database;
import ru.istu.police.ui.ConsoleApp;

/**
 * Точка входа в приложение учёта задержаний.
 */
public class Main {

    public static void main(String[] args) {
        ConsoleApp app = new ConsoleApp();
        try {
            app.run();
        } finally {
            Database.close();
        }
    }
}
