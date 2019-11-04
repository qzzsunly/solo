package cn.sexycode.util.core.properties;

import cn.sexycode.util.core.collection.ArrayHelper;
import cn.sexycode.util.core.str.StringUtils;

import javax.naming.ConfigurationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

public class PropertiesUtil {
    public static void overrideProperties(Properties properties, Map<?, ?> overrides) {
        for (Map.Entry entry : overrides.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                properties.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Get the config value as a {@link String}
     *
     * @param name   The config setting name.
     * @param values The map of config values
     * @return The value, or null if not found
     */
    public static String getString(String name, Map values) {
        Object value = values.get(name);
        if (value == null) {
            return null;
        }
        if (String.class.isInstance(value)) {
            return (String) value;
        }
        return value.toString();
    }

    /**
     * Get the config value as a {@link String}
     *
     * @param name         The config setting name.
     * @param values       The map of config values
     * @param defaultValue The default value to use if not found
     * @return The value.
     */
    public static String getString(String name, Map values, String defaultValue) {
        final String value = getString(name, values);
        return value == null ? defaultValue : value;
    }

    /**
     * Get the config value as a {@link String}.
     *
     * @param name                 The config setting name.
     * @param values               The map of config parameters.
     * @param defaultValue         The default value to use if not found.
     * @param otherSupportedValues List of other supported values. Does not need to contain the default one.
     * @return The value.
     * @throws ConfigurationException Unsupported value provided.
     */
    public static String getString(String name, Map values, String defaultValue, String... otherSupportedValues) {
        final String value = getString(name, values, defaultValue);
        if (!defaultValue.equals(value) && ArrayHelper.indexOf(otherSupportedValues, value) == -1) {
            throw new IllegalArgumentException(
                    "Unsupported configuration [name=" + name + ", value=" + value + "]. " + "Choose value between: '"
                            + defaultValue + "', '" + StringUtils.join("', '", otherSupportedValues) + "'.");
        }
        return value;
    }

    /**
     * Get the config value as a boolean (default of false)
     *
     * @param name   The config setting name.
     * @param values The map of config values
     * @return The value.
     */
    public static boolean getBoolean(String name, Map values) {
        return getBoolean(name, values, false);
    }

    /**
     * Get the config value as a boolean.
     *
     * @param name         The config setting name.
     * @param values       The map of config values
     * @param defaultValue The default value to use if not found
     * @return The value.
     */
    public static Boolean getBoolean(String name, Map values, boolean defaultValue) {
        Object value = values.get(name);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return null;
    }

    /**
     * Get the config value as an int
     *
     * @param name         The config setting name.
     * @param values       The map of config values
     * @param defaultValue The default value to use if not found
     * @return The value.
     */
    public static int getInt(String name, Map values, int defaultValue) {
        Object value = values.get(name);
        if (value == null) {
            return defaultValue;
        }
        if (Integer.class.isInstance(value)) {
            return (Integer) value;
        }
        if (String.class.isInstance(value)) {
            return Integer.parseInt((String) value);
        }
        throw new IllegalArgumentException(
                "Could not determine how to handle configuration value [name=" + name + ", value=" + value + "(" + value
                        .getClass().getName() + ")] as int");
    }

    /**
     * Get the config value as an {@link Integer}
     *
     * @param name   The config setting name.
     * @param values The map of config values
     * @return The value, or null if not found
     */
    public static Integer getInteger(String name, Map values) {
        Object value = values.get(name);
        if (value == null) {
            return null;
        }
        if (Integer.class.isInstance(value)) {
            return (Integer) value;
        }
        if (String.class.isInstance(value)) {
            //empty values are ignored
            final String trimmed = value.toString().trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            return Integer.valueOf(trimmed);
        }
        return null;
    }

    public static long getLong(String name, Map values, int defaultValue) {
        Object value = values.get(name);
        if (value == null) {
            return defaultValue;
        }
        if (Long.class.isInstance(value)) {
            return (Long) value;
        }
        if (String.class.isInstance(value)) {
            return Long.parseLong((String) value);
        }
        throw new IllegalArgumentException(
                "Could not determine how to handle configuration value [name=" + name + ", value=" + value + "(" + value
                        .getClass().getName() + ")] as long");
    }

    /**
     * Make a clone of the configuration values.
     *
     * @param configurationValues The config values to clone
     * @return The clone
     */
    @SuppressWarnings({ "unchecked" })
    public static Map clone(Map<?, ?> configurationValues) {
        if (configurationValues == null) {
            return null;
        }
        // If a Properties object, leverage its clone() impl
        if (Properties.class.isInstance(configurationValues)) {
            return (Properties) ((Properties) configurationValues).clone();
        }
        // Otherwise make a manual copy
        HashMap clone = new HashMap();
        for (Map.Entry entry : configurationValues.entrySet()) {
            clone.put(entry.getKey(), entry.getValue());
        }
        return clone;
    }

    /**
     * replace a property by a starred version
     *
     * @param props properties to check
     * @param key   proeprty to mask
     * @return cloned and masked properties
     */
    public static Properties maskOut(Properties props, String key) {
        Properties clone = (Properties) props.clone();
        if (clone.get(key) != null) {
            clone.setProperty(key, "****");
        }
        return clone;
    }

    /**
     * Extract a property value by name from the given properties object.
     * <p/>
     * Both <tt>null</tt> and <tt>empty string</tt> are viewed as the same, and return null.
     *
     * @param propertyName The name of the property for which to extract value
     * @param properties   The properties object
     * @return The property value; may be null.
     */
    public static String extractPropertyValue(String propertyName, Properties properties) {
        String value = properties.getProperty(propertyName);
        if (value == null) {
            return null;
        }
        value = value.trim();
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return value;
    }

    /**
     * Extract a property value by name from the given properties object.
     * <p/>
     * Both <tt>null</tt> and <tt>empty string</tt> are viewed as the same, and return null.
     *
     * @param propertyName The name of the property for which to extract value
     * @param properties   The properties object
     * @return The property value; may be null.
     */
    public static String extractPropertyValue(String propertyName, Map properties) {
        String value = (String) properties.get(propertyName);
        if (value == null) {
            return null;
        }
        value = value.trim();
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return value;
    }

    /**
     * Constructs a map from a property value.
     * <p/>
     * The exact behavior here is largely dependant upon what is passed in as
     * the delimiter.
     *
     * @param propertyName The name of the property for which to retrieve value
     * @param delim        The string defining tokens used as both entry and key/value delimiters.
     * @param properties   The properties object
     * @return The resulting map; never null, though perhaps empty.
     * @see #extractPropertyValue(String, Properties)
     */
    public static Map toMap(String propertyName, String delim, Properties properties) {
        Map map = new HashMap();
        String value = extractPropertyValue(propertyName, properties);
        if (value != null) {
            StringTokenizer tokens = new StringTokenizer(value, delim);
            while (tokens.hasMoreTokens()) {
                map.put(tokens.nextToken(), tokens.hasMoreElements() ? tokens.nextToken() : "");
            }
        }
        return map;
    }

    /**
     * Constructs a map from a property value.
     * <p/>
     * The exact behavior here is largely dependant upon what is passed in as
     * the delimiter.
     *
     * @param propertyName The name of the property for which to retrieve value
     * @param delim        The string defining tokens used as both entry and key/value delimiters.
     * @param properties   The properties object
     * @return The resulting map; never null, though perhaps empty.
     * @see #extractPropertyValue(String, Properties)
     */
    public static Map toMap(String propertyName, String delim, Map properties) {
        Map map = new HashMap();
        String value = extractPropertyValue(propertyName, properties);
        if (value != null) {
            StringTokenizer tokens = new StringTokenizer(value, delim);
            while (tokens.hasMoreTokens()) {
                map.put(tokens.nextToken(), tokens.hasMoreElements() ? tokens.nextToken() : "");
            }
        }
        return map;
    }

    /**
     * Get a property value as a string array.
     *
     * @param propertyName The name of the property for which to retrieve value
     * @param delim        The delimiter used to separate individual array elements.
     * @param properties   The properties object
     * @return The array; never null, though may be empty.
     * @see #extractPropertyValue(String, Properties)
     * @see #toStringArray(String, String)
     */
    public static String[] toStringArray(String propertyName, String delim, Properties properties) {
        return toStringArray(extractPropertyValue(propertyName, properties), delim);
    }

    /**
     * Convert a string to an array of strings.  The assumption is that
     * the individual array elements are delimited in the source stringForm
     * param by the delim param.
     *
     * @param stringForm The string form of the string array.
     * @param delim      The delimiter used to separate individual array elements.
     * @return The array; never null, though may be empty.
     */
    public static String[] toStringArray(String stringForm, String delim) {
        // todo : move to StringHelper?
        if (stringForm != null) {
            return StringUtils.split(delim, stringForm);
        } else {
            return ArrayHelper.EMPTY_STRING_ARRAY;
        }
    }
}
