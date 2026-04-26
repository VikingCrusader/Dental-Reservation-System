import database.DatabaseManager;
import gui.WelcomePage;

import javax.swing.*;

/**
 * Application entry point.
 *
 * Startup sequence:
 *   1. Initialize SQLite database (creates dental.db + tables if missing).
 *   2. Register a shutdown hook to close the DB connection cleanly.
 *   3. Launch the Swing login screen on the Event Dispatch Thread.
 *
 * The database file (dental.db) is created in the project root directory.
 * You can open it in DataGrip to browse data visually.
 */
public class Main {

    public static void main(String[] args) {

        // 1. Set up DB — safe to call multiple times (CREATE TABLE IF NOT EXISTS)
        DatabaseManager.initializeDatabase();

        // 2. Close DB connection when the JVM exits
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseManager::closeConnection));

        // 3. Launch GUI on the Event Dispatch Thread (Swing best practice)
        SwingUtilities.invokeLater(WelcomePage::login);
    }
}
