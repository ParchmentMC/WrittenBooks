package org.parchmentmc.writtenbooks;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.parchmentmc.writtenbooks.publishing.PublishingManager;
import org.parchmentmc.writtenbooks.versioning.GitVersion;

public class WrittenBooksPlugin implements Plugin<Project> {
    public static final String EXTENSION_NAME = "writtenbooks";

    public void apply(Project project) {
        final WrittenBooksExtension extension = project.getExtensions().create(EXTENSION_NAME, WrittenBooksExtension.class);

        project.getLogger().debug("Applying Git-based versioning");
        project.setVersion(new GitVersion(project, extension.getMainBranches(), true));

        project.getLogger().debug("Applying repository");
        project.getRepositories().maven(repo -> {
            repo.setName("ParchmentMC");
            repo.setUrl("https://ldtteam.jfrog.io/artifactory/parchmentmc/");
        });

        final Provider<String> repo = extension.getReleaseRepository().zip(extension.getSnapshotRepository(),
                (release, snapshot) -> {
                    if (project.getVersion() instanceof GitVersion) {
                        return ((GitVersion) project.getVersion()).isSnapshot() ? snapshot : release;
                    }
                    return snapshot;
                });

        project.getLogger().debug("Applying publishing manager.");
        new PublishingManager(repo, extension.getRepositoryUsername(), extension.getRepositoryPassword()).apply(project);
    }
}
