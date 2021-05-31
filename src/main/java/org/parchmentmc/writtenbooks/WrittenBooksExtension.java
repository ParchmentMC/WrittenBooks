package org.parchmentmc.writtenbooks;

import org.gradle.api.provider.ListProperty;
import org.parchmentmc.writtenbooks.versioning.GitVersion;

public abstract class WrittenBooksExtension {
    public WrittenBooksExtension() {
        getMainBranches().convention(GitVersion.DEFAULT_MAIN_BRANCHES);
    }

    /**
     * Returns the list of branches which are exempt from the branch affixing in {@link
     * org.parchmentmc.writtenbooks.versioning.GitVersion}.
     *
     * @return the list of exempt branches
     */
    public abstract ListProperty<String> getMainBranches();
}
