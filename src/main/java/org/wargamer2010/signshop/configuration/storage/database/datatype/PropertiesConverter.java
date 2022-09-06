package org.wargamer2010.signshop.configuration.storage.database.datatype;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.*;

@Converter(autoApply = true)
public class PropertiesConverter implements AttributeConverter<Properties, String> {
    private static final String keyValueDelimeter = "=";
    private static final String entryDelimeter = ";";

    @Override
    public String convertToDatabaseColumn(Properties properties) {
        if (properties == null) return null;

        List<String> entries = new ArrayList<>();

        properties.forEach((k,v) -> entries.add(k + keyValueDelimeter + v));

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
