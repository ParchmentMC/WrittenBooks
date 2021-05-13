package org.parchmentmc.writtenbooks.versioning;

import org.gradle.api.Project;

public class VersioningManager {
    private static final VersioningManager INSTANCE = new VersioningManager();

    public static VersioningManager getInstance() {
        return INSTANCE;
    }

    private VersioningManager() {
    }

    public void apply(final Project project) {
        project.setVersion(new GitVersion(project));
    }
}
