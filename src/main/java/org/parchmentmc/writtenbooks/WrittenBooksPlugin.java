package org.parchmentmc.writtenbooks;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.parchmentmc.writtenbooks.publishing.PublishingManager;
import org.parchmentmc.writtenbooks.versioning.GitVersion;

import java.util.function.BiFunction;

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

        final Provider<String> publicationRepo = extension.getBleedingRepository().zip(
          extension.getSnapshotRepository(),
          (bleeding, snapshot) -> {
              if (extension.getBleedingVersion().isPresent()) {
                  return extension.getBleedingVersion().get() ? bleeding : snapshot;
              }

              return snapshot;
          }
        ).zip(extension.getReleaseRepository(), (snapshotOrBleeding, release) -> {
            //If bleeding is present the previous zip will have returned us the bleeding repository.
            //Blind passthroughs.
            if (extension.getBleedingVersion().isPresent() && extension.getBleedingVersion().get())
                return snapshotOrBleeding;

            //In any other case, snapshotOrBleeding will always be the snapshot repository.
            //We as such validate if this is a snapshot build and execute it accordingly.
            if (extension.getSnapshotVersion().isPresent()) {
                return extension.getSnapshotVersion().get() ? snapshotOrBleeding : release;
            }
            if (project.getVersion() instanceof GitVersion) {
                return ((GitVersion) project.getVersion()).isSnapshot() ? snapshotOrBleeding : release;
            }
            return snapshotOrBleeding;
        });

        project.getLogger().debug("Applying publishing manager");
        new PublishingManager(publicationRepo, extension.getRepositoryUsername(), extension.getRepositoryPassword(),
                extension.getGithubRepo()).apply(project);
    }
}
