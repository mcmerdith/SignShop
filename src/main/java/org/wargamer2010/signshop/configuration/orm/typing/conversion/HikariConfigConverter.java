package org.wargamer2010.signshop.configuration.orm.typing.conversion;

import com.zaxxer.hikari.HikariConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.wargamer2010.signshop.util.StringUtils.*;

public class HikariConfigConverter implements SSAttributeConverter<HikariConfig, String> {
    /**
     * Convert a HikariConfig to a String formatted as
     * url;username;password[;prop1=val1;prop2=val2;...]
     *
     * @param config The config to convert
     * @return The converted String
     */
    @Override
    public String convertToDatabaseColumn(HikariConfig config) {
        return configToString(config);
    }

    public static String configToString(HikariConfig config) {
        // Return if there is no config
        if (config == null) return null;

        List<String> components = new ArrayList<>();

        components.add(escapeProperties(config.getJdbcUrl()));
        components.add(escapeProperties(config.getUsername()));
        components.add(escapeProperties(config.getPassword()));

        // Add all the properties
        config.getDataSourceProperties().forEach((k, v) ->
                components.add(
                        escapeProperties(k.toString())+
                                propertyDelimiter +
                                escapeProperties(v.toString())
                ));

        return String.join(delimiter, components);
    }

    /**
     * Convert a String formatted as
     * url;username;password[;prop1=val1;prop2=val2;...]
     * to a HikariConfig
     *
     * @param props The String to convert
     * @return The converted HikariConfig
     */
    @Override
    public HikariConfig convertToModelAttribute(String props) {
        return configFromString(props);
    }

    public static HikariConfig configFromString(String props) {
        HikariConfig output = new HikariConfig();

        // Return if no properties were provided
        if (props == null || props.trim().length() < 3) return output;

        // Split up the string
        String[] entries = props.trim().split(delimiter);

        // The first parameter is the URL
        output.setJdbcUrl(unescapeProperties(entries[0]));

        // The second parameter is the Username
        output.setUsername(unescapeProperties(entries[1]));

        // The third parameter is the Password
        output.setPassword(unescapeProperties(entries[2]));

        // The remaining parameters are dataSource properties
        Properties dataSourceProperties = new Properties();

        // Iterate through the remaining elements (if any)
        for (int i = 3; i < entries.length; i++) {
            String[] keyValue = entries[i].split(propertyDelimiter);
            if (keyValue.length < 2) continue;
            dataSourceProperties.setProperty(unescapeProperties(keyValue[0]), unescapeProperties(keyValue[1]));
        }

        // Update the properties
        output.setDataSourceProperties(dataSourceProperties);

        // All parameters parsed!
        return output;
    }
}
