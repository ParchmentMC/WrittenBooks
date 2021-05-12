package org.parchmentmc.writtenbooks.versioning;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.api.Project;
import org.gradle.api.logging.LogLevel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VersioningManager
{
    private static final VersioningManager INSTANCE = new VersioningManager();

    public static VersioningManager getInstance()
    {
        return INSTANCE;
    }

    private static final List<String> DEFAULT_MAIN_BRANCHES = new ArrayList<>();
    static {
        DEFAULT_MAIN_BRANCHES.add("master");
        DEFAULT_MAIN_BRANCHES.add("main");
        DEFAULT_MAIN_BRANCHES.add("HEAD");
    }

    private VersioningManager()
    {
    }

    public void apply(final Project project) {
        final String version = getVersionFromProject(project);
        project.setVersion(version);

        project.getLogger().info("Updated version of project: " + project.getName() + " to: " + version);
    }

    private String getVersionFromProject(final Project project) {
        final Git projectGit = getGitFromProject(project);

        try
        {
            final String[] description = projectGit.describe().setLong(true).setTags(true).call().split("-");
            String branch = projectGit.getRepository().getBranch();
            if (branch != null && branch.startsWith("pulls/"))
                branch = "pr" + branch.split("/", 1)[1];
            if (branch == null || DEFAULT_MAIN_BRANCHES.contains(branch))
                return description[0] + "." + description[1];
            return description[0] + "." + description[1] + "-" + branch;
        }
        catch (IOException | GitAPIException e)
        {
            project.getLogger().log(LogLevel.ERROR, "Failure to determine version string.", e);
            return "0.0.0-FAILURE";
        }
    }

    private Git getGitFromProject(final Project project) {
        return getGitFromDirectory(project.getProjectDir());
    }

    private Git getGitFromDirectory(final File directory) throws IllegalArgumentException {
        try
        {
            return Git.open(directory);
        }
        catch (Exception e)
        {
            if (directory.getParentFile() == null)
                throw new IllegalArgumentException("Could not find the git workspace of the current directory.");

            return getGitFromDirectory(directory.getParentFile());
        }
    }


}
