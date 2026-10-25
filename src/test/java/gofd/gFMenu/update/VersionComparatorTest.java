package gofd.gFMenu.update;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionComparatorTest {
    @Test
    void comparesReleaseNumbersAndPrefixes() {
        assertEquals(0, VersionComparator.compare("R1.2.0", "1.2"));
        assertTrue(VersionComparator.compare("1.10.0", "1.9.9") > 0);
        assertTrue(VersionComparator.compare("1.2.0", "1.2.0-alpha") > 0);
        assertTrue(VersionComparator.compare("R1.1.5-hotfix", "R1.1.5") > 0);
        assertTrue(VersionComparator.compare("1.1.5-hotfix.10", "1.1.5-hotfix.2") > 0);
    }
}
