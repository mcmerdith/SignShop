package org.wargamer2010.signshop.configuration.storage.database.datatype;

import org.wargamer2010.signshop.configuration.annotations.Converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Converter
public class PropertiesConverter implements SSAttributeConverter<Properties, String> {
    private static final String keyValueDelimeter = "=";
    private static final String entryDelimeter = ";";

    @Override
    public String convertToDatabaseColumn(Properties properties) {
        if (properties == null) return null;

        List<String> entries = new ArrayList<>();

        properties.forEach((k, v) -> entries.add(k + keyValueDelimeter + v));

        return String.join(entryDelimeter, entries);
    }

    @Override
    public Properties convertToEntityAttribute(String props) {
        Properties output = new Properties();

        if (props == null) return output;

        String[] entries = props.split(entryDelimeter);
        for (String entry : entries) {
            String[] keyValue = entry.split(keyValueDelimeter);
            if (keyValue.length >= 2) output.put(keyValue[0], keyValue[1]);
        }

        return output;
    }
}
