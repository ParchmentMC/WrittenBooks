package org.parchmentmc.writtenbooks;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ProviderFactory;
import org.parchmentmc.writtenbooks.versioning.GitVersion;

import javax.inject.Inject;

public abstract class WrittenBooksExtension {
    @Inject
    public WrittenBooksExtension(ProviderFactory providers) {
        getMainBranches().convention(GitVersion.DEFAULT_MAIN_BRANCHES);
        getReleaseRepository().convention("https://ldtteam.jfrog.io/artifactory/parchmentmc-internal/");
        getSnapshotRepository().convention("https://ldtteam.jfrog.io/artifactory/parchmentmc-snapshots/");
        getRepositoryUsername().convention(providers.environmentVariable("LDTTeamJfrogUsername"));
        getRepositoryPassword().convention(providers.environmentVariable("LDTTeamJfrogPassword"));
    }

    /**
     * The list of branches which are exempt from the branch affixing in {@link
     * org.parchmentmc.writtenbooks.versioning.GitVersion}.
     */
    public abstract ListProperty<String> getMainBranches();

    /**
     * The URL for the Maven repository where releases are published.
     */
    public abstract Property<String> getReleaseRepository();

    /**
     * The URL for the Maven repository where snapshots are published.
     */
    public abstract Property<String> getSnapshotRepository();

    /**
     * The username used for the authenticating to the publishing repository.
     *
     * <p>If either this or the {@linkplain #getRepositoryPassword() repository password} are not specified, then
     * the publishing repository is not registered.</p>
     */
    public abstract Property<String> getRepositoryUsername();

    /**
     * The password used for the authenticating to the publishing repository.
     *
     * <p>If either this or the {@linkplain #getRepositoryUsername() repository username} are not specified, then
     * the publishing repository is not registered.</p>
     */
    public abstract Property<String> getRepositoryPassword();

    /**
     * The GitHub repository where this project is located, in the form of {@code username/repository}. Used for default
     * values in {@link org.gradle.api.publish.maven.MavenPom POMs} in Maven publications.
     */
    public abstract Property<String> getGithubRepo();
}
