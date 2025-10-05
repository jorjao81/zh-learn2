package com.zhlearn.cli;

import java.util.Map;
import java.util.HashMap;

/**
 * Centralized configuration for the zh-learn CLI application.
 * Provides access to system properties, environment variables, and default settings.
 */
public class Configuration {

    private final Map<String, String> properties;

    private Configuration(Map<String, String> properties) {
        this.properties = new HashMap<>(properties);
    }

    /**
     * Create default configuration from system properties and environment variables.
     */
    public static Configuration defaultConfig() {
        Map<String, String> props = new HashMap<>();

        // Add system properties
        props.putAll(System.getProperties().entrySet().stream()
            .collect(HashMap::new,
                (m, e) -> m.put(e.getKey().toString(), e.getValue().toString()),
                HashMap::putAll));

        // Add environment variables with ZHLEARN_ prefix
        Map<String, String> envVars = System.getenv();
        envVars.entrySet().stream()
            .filter(e -> e.getKey().startsWith("ZHLEARN_"))
            .forEach(e -> props.put(e.getKey(), e.getValue()));

        return new Configuration(props);
    }

    /**
     * Create configuration with specific properties (for testing).
     */
    public static Configuration of(Map<String, String> properties) {
        return new Configuration(properties);
    }

    /**
     * Get a property value, checking system properties first, then environment variables.
     */
    public String getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Get a property value with default.
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }

    /**
     * Get a boolean property value.
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value);
    }

    /**
     * Get an integer property value.
     */
    public int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // Configuration accessors for commonly used settings
    public String getAnkiMediaDirectory() {
        return getProperty("ZHLEARN_ANKI_MEDIA_DIR");
    }

    public String getZhLearnHome() {
        return getProperty("zhlearn.home");
    }

    public boolean isDebugEnabled() {
        return getBooleanProperty("zhlearn.debug", false);
    }

    public int getTerminalWidth() {
        return getIntProperty("zhlearn.terminal.width", 80);
    }

    public boolean isColorEnabled() {
        return getBooleanProperty("zhlearn.color", true);
    }
}