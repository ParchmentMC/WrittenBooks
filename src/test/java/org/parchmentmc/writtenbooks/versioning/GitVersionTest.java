package org.parchmentmc.writtenbooks.versioning;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.parchmentmc.writtenbooks.versioning.GitVersion.DEFAULT_MAIN_BRANCHES;
import static org.parchmentmc.writtenbooks.versioning.GitVersion.createVersionString;

public class GitVersionTest {

    @Test
    public void testVersionStrings() {
        assertEquals("1.0.0",
                createVersionString("1.0.0", 0, "main", DEFAULT_MAIN_BRANCHES::contains));
        assertEquals("1.0.0-SNAPSHOT",
                createVersionString("1.0.0", 25, "main", DEFAULT_MAIN_BRANCHES::contains));
        assertEquals("1.0.0",
                createVersionString("1.0.0", 0, "dev", DEFAULT_MAIN_BRANCHES::contains));
        assertEquals("1.0.0-dev-SNAPSHOT",
                createVersionString("1.0.0", 25, "dev", DEFAULT_MAIN_BRANCHES::contains));
        assertEquals("1.0.0-main-SNAPSHOT",
                createVersionString("1.0.0", 25, "main", s -> false));

        assertEquals("1.1.0",
                createVersionString("v1.1.0", 0, "main", DEFAULT_MAIN_BRANCHES::contains));
        assertEquals("1.1.0-SNAPSHOT",
                createVersionString("v1.1.0", 25, "main", DEFAULT_MAIN_BRANCHES::contains));
        assertEquals("1.1.0",
                createVersionString("v1.1.0", 0, "dev", DEFAULT_MAIN_BRANCHES::contains));
        assertEquals("1.1.0-dev-SNAPSHOT",
                createVersionString("v1.1.0", 25, "dev", DEFAULT_MAIN_BRANCHES::contains));
        assertEquals("1.1.0-main-SNAPSHOT",
                createVersionString("v1.1.0", 25, "main", s -> false));
    }
}
