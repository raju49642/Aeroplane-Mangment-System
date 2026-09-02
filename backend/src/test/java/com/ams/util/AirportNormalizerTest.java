package com.ams.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AirportNormalizerTest {
    @Test
    void acceptsCodesAndAlternativeCitySpellings() {
        assertEquals("Hyderabad", AirportNormalizer.normalize("  hyd "));
        assertEquals("Bengaluru", AirportNormalizer.normalize("BANGALORE"));
        assertEquals("Ahmedabad", AirportNormalizer.normalize("amd"));
    }

    @Test
    void searchesAcrossCanonicalNamesCodesAndAliases() {
        assertTrue(AirportNormalizer.searchTerms("BLR").containsAll(
                java.util.List.of("bengaluru", "bangalore", "blr")));
        assertTrue(AirportNormalizer.searchTerms("hyd").containsAll(
                java.util.List.of("hyderabad", "hyd")));
    }
}
