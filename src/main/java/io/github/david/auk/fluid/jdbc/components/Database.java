package io.github.david.auk.fluid.jdbc.components;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Simple JDBC connection factory.
 * <p>
 * Important for tests: do NOT cache URL/USER/PASSWORD in static finals based on System properties,
 * because those can change between test classes (e.g. when running multiple Testcontainers DBs).
 */
public final class Database {

    /** Properties file values loaded once (lowest precedence). */
    private static final Properties CLASSPATH_PROPS = loadClasspathProps();

    private Database() {
        // Utility class
    }

    /**
     * Opens a new JDBC connection.
     */
    public static Connection getConnection(String url, String user, String password) {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Opens a new JDBC connection using resolved datasource configuration.
     * <p>
     * Precedence: System properties → Environment variables → datasource.properties.
     */
    public static Connection getConnection() {
        DatasourceConfig config = resolveConfig();
        return getConnection(config.url(), config.user(), config.password());
    }

    /**
     * Resolves datasource configuration at call time.
     * <p>
     * This intentionally re-reads System properties each time so Testcontainers-backed tests can
     * change datasource.url/username/password between test classes in the same JVM.
     */
    private static DatasourceConfig resolveConfig() {
        String url = firstNonBlank(
                System.getProperty("datasource.url"),
                System.getenv("DATASOURCE_URL"),
                CLASSPATH_PROPS.getProperty("datasource.url")
        );

        String user = firstNonBlank(
                System.getProperty("datasource.username"),
                System.getenv("DATASOURCE_USERNAME"),
                CLASSPATH_PROPS.getProperty("datasource.username")
        );

        String password = firstNonBlank(
                System.getProperty("datasource.password"),
                System.getenv("DATASOURCE_PASSWORD"),
                CLASSPATH_PROPS.getProperty("datasource.password")
        );

        if (isBlank(url) || isBlank(user)) {
            throw new IllegalStateException(
                    "Missing datasource configuration. Provide either:\n" +
                            "- datasource.properties with datasource.url and datasource.username (+ password)\n" +
                            "- or system properties datasource.*\n" +
                            "- or env vars DATASOURCE_*"
            );
        }

        return new DatasourceConfig(url, user, password);
    }

    private static Properties loadClasspathProps() {
        Properties props = new Properties();

        // Load datasource.properties from classpath if present
        try (InputStream in = Database.class.getClassLoader().getResourceAsStream("datasource.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load datasource.properties", e);
        }

        return props;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (!isBlank(v)) return v;
        }
        return null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private record DatasourceConfig(String url, String user, String password) {}
}
