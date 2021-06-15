package org.parchmentmc.writtenbooks;

import org.gradle.api.Project;
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
        getRepositoryUsername().convention(providers.environmentVariable("LDTTeamJfrogUsername").forUseAtConfigurationTime());
        getRepositoryPassword().convention(providers.environmentVariable("LDTTeamJfrogPassword").forUseAtConfigurationTime());
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

    /**
     * An override for whether this version is a snapshot version, used to determine what publishing repository to use.
     *
     * <p>The logic for what repository is used for publishing is as follows (assuming that the repository
     * {@link #getRepositoryUsername() username} and {@link #getRepositoryPassword() password} are specified):</p>
     * <ul>
     *     <li>First, the value of this property is queried. If there is a value, then if it is {@code true}, the
     *     snapshot repository is used, otherwise if {@code false} then the release repository is used.</li>
     *     <li>If the property has no value, then the {@link Project#getVersion() version object} is checked if it
     *     is the {@link GitVersion version supplied by WrittenBooks}. If so, then the version object determines whether
     *     to use the snapshot repository through {@link GitVersion#isSnapshot()}.</li>
     *     <li>Otherwise, the snapshot repository is used.</li>
     * </ul>
     *
     * @see #getReleaseRepository()
     * @see #getSnapshotRepository()
     */
    public abstract Property<Boolean> getSnapshotVersion();
}
