package com.ams.util;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/** Normalizes city names and IATA codes at the API boundary. */
public final class AirportNormalizer {
    private static final Map<String, String> CANONICAL_NAMES = Map.ofEntries(
            Map.entry("del", "Delhi"), Map.entry("bom", "Mumbai"),
            Map.entry("blr", "Bengaluru"), Map.entry("bangalore", "Bengaluru"), Map.entry("bengaluru", "Bengaluru"),
            Map.entry("maa", "Chennai"), Map.entry("ccu", "Kolkata"), Map.entry("calcutta", "Kolkata"),
            Map.entry("pnq", "Pune"), Map.entry("hyd", "Hyderabad"), Map.entry("cok", "Kochi"), Map.entry("cochin", "Kochi"),
            Map.entry("ixc", "Chandigarh"), Map.entry("jai", "Jaipur"), Map.entry("amd", "Ahmedabad"),
            Map.entry("lko", "Lucknow"), Map.entry("idr", "Indore"), Map.entry("goi", "Goa"), Map.entry("sxr", "Srinagar"));

    private AirportNormalizer() { }

    public static String normalize(String airport) {
        String key = airport.trim().toLowerCase(Locale.ROOT);
        return CANONICAL_NAMES.getOrDefault(key, airport.trim());
    }

    public static List<String> searchTerms(String airport) {
        String canonical = normalize(airport).toLowerCase(Locale.ROOT);
        return Stream.concat(
                        Stream.of(canonical),
                        CANONICAL_NAMES.entrySet().stream()
                                .filter(entry -> entry.getValue().equalsIgnoreCase(canonical))
                                .map(Map.Entry::getKey))
                .distinct()
                .toList();
    }
}
