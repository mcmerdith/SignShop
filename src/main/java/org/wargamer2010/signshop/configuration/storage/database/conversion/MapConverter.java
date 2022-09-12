package org.wargamer2010.signshop.configuration.storage.database.conversion;

import org.wargamer2010.signshop.configuration.orm.typing.SSAttributeConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapConverter implements SSAttributeConverter<Map<String, String>, List<String>> {
    private static final String keyValueDelimeter = "=";

    @Override
    public List<String> convertToDatabaseColumn(Map<String, String> stringStringMap) {
        List<String> entries = new ArrayList<>();

        if (stringStringMap == null) return entries;

        stringStringMap.forEach((k, v) -> entries.add(k + keyValueDelimeter + v));

        return entries;
    }

    @Override
    public Map<String, String> convertToModelAttribute(List<String> s) {
        Map<String, String> output = new HashMap<>();

        if (s == null || s.isEmpty()) return output;

        for (String entry : s) {
            String[] keyValue = entry.split(keyValueDelimeter);
            if (keyValue.length >= 2) output.put(keyValue[0], keyValue[1]);
        }

        return output;
    }
}
