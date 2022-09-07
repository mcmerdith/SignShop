package org.wargamer2010.signshop.configuration.storage.database.datatype;

import org.wargamer2010.signshop.configuration.annotations.Converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Converter
public class MapConverter implements SSAttributeConverter<Map<String, String>, String> {
    private static final String keyValueDelimeter = ":";
    private static final String entryDelimeter = ";";

    @Override
    public String convertToDatabaseColumn(Map<String, String> stringStringMap) {
        if (stringStringMap == null) return "";

        List<String> entries = new ArrayList<>();

        stringStringMap.forEach((k, v) -> entries.add(k + keyValueDelimeter + v));

        return String.join(entryDelimeter, entries);
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String s) {
        Map<String, String> output = new HashMap<>();

        if (s == null) return output;

        String[] entries = s.split(entryDelimeter);
        for (String entry : entries) {
            String[] keyValue = entry.split(keyValueDelimeter);
            if (keyValue.length >= 2) output.put(keyValue[0], keyValue[1]);
        }

        return output;
    }
}
