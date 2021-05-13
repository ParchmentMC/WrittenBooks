package org.parchmentmc.writtenbooks.versioning;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
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

    public GitVersion(Project project, List<String> exemptBranches) {
        this.project = project;
        this.exemptBranches = exemptBranches;
    }

    public GitVersion(Project project) {
        this(project, DEFAULT_MAIN_BRANCHES);
    }

    public String getVersion() {
        if (cachedVersion == null) {
            cachedVersion = getVersionFromProject(project);
            project.getLogger().lifecycle("Version for {}: {}", project.getName(), cachedVersion);
        }
        return cachedVersion;
    }

    private String getVersionFromProject(final Project project) {
        final Git projectGit = getGitFromProject(project);

        if (projectGit == null) {
            return "0.0.0-NOGIT";
        }

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
            project.getLogger().log(LogLevel.ERROR, "Failure to determine version string.", e);
            return "0.0.0-FAILURE";
        }
    }

    private Git getGitFromProject(final Project project) {
        return getGitFromDirectory(project.getProjectDir());
    }

    private Git getGitFromDirectory(final File directory) throws IllegalArgumentException {
        try {
            return Git.open(directory);
        } catch (Exception e) {
            if (directory.getParentFile() == null)
                throw new IllegalArgumentException("Could not find the git workspace of the current directory.");

            return getGitFromDirectory(directory.getParentFile());
        }
    }

    @Override
    public String toString() {
        return getVersion();
    }
}
