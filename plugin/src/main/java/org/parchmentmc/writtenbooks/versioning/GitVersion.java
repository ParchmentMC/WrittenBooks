package org.parchmentmc.writtenbooks.versioning;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.gradle.api.Project;
import org.gradle.api.logging.LogLevel;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitVersion {
    private static final List<String> DEFAULT_MAIN_BRANCHES = new ArrayList<>();

    static {
        DEFAULT_MAIN_BRANCHES.add("master");
        DEFAULT_MAIN_BRANCHES.add("main");
        DEFAULT_MAIN_BRANCHES.add("HEAD");
    }

    private final Project project;
    private final List<String> exemptBranches;
    @Nullable
    private String cachedVersion = null;
    private final boolean throwOnError;

    public GitVersion(Project project, List<String> exemptBranches, boolean throwOnError) {
        this.project = project;
        this.exemptBranches = exemptBranches;
        this.throwOnError = throwOnError;
    }

    public GitVersion(Project project) {
        this(project, DEFAULT_MAIN_BRANCHES, true);
    }

    public String getVersion() {
        if (cachedVersion == null) {
            cachedVersion = calculateVersion(project);
            project.getLogger().lifecycle("Version for {}: {}", project.getName(), cachedVersion);
        }
        return cachedVersion;
    }

    private String calculateVersion(final Project project) {
        try (Repository repo = getRepository(project, throwOnError)) {
            if (repo == null) {
                return "0.0.0-NOGIT";
            }

            Git projectGit = Git.wrap(repo);

            try {
                final String desc = projectGit.describe().setLong(true).setTags(true).call();
                if (desc == null)
                    return "0.0.0-NODESC";

                final String[] descParts = projectGit.describe().setLong(true).setTags(true).call().split("-");

                final int offset = Integer.parseInt(descParts[1]);

                String branch = projectGit.getRepository().getBranch();
                if (branch != null && branch.startsWith("pulls/"))
                    branch = "pr" + branch.split("/", 1)[1];
                if (branch == null || exemptBranches.contains(branch)) {
                    if (offset == 0)
                        return descParts[0];

                    return descParts[0] + "." + descParts[1];
                }

                if (offset == 0) {
                    return descParts[0] + "-" + branch + "." + offset;
                }
                return descParts[0] + "." + descParts[1] + "-" + branch;
            } catch (IOException | GitAPIException e) {
                if (throwOnError) {
                    throw new IllegalArgumentException("Failed to determine version string from Git", e);
                } else {
                    project.getLogger().log(LogLevel.ERROR, "Failure to determine version string from Git", e);
                }
                return "0.0.0-FAILURE";
            }
        }
    }

    @Nullable
    private Repository getRepository(final Project project, boolean throwOnError) {
        return getRepository(project.getProjectDir(), throwOnError);
    }

    @Nullable
    private Repository getRepository(final File directory, boolean throwOnError) throws IllegalArgumentException {
        try {
            return new RepositoryBuilder()
                    .readEnvironment()
                    .findGitDir(directory)
                    .build();
        } catch (IOException e) {
            if (throwOnError) {
                throw new IllegalArgumentException("Could not find Git repository starting from " + directory, e);
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return getVersion();
    }
}
