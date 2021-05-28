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
import java.util.function.Predicate;

public class GitVersion {
    private static final String NO_GIT_VERSION = "0.0.0-NOGIT";
    private static final String NO_GIT_DESCRIBE_VERSION = "0.0.0-DESCRIBE";
    private static final String GENERAL_FAILURE_VERSION = "0.0.0-FAILURE";
    private static final String EXEMPT_BRANCH_VERSION = "%s.%s-SNAPSHOT";
    private static final String BRANCH_VERSION = "%s.%s-%s-SNAPSHOT";
    public static final List<String> DEFAULT_MAIN_BRANCHES = new ArrayList<>();

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

    /*
    Versioning logic is as follows:
     [for tags, remove the `v` from the beginning if present]
     - if the commit is tagged, use the version from the tag directly
     - otherwise:
       - if the current branch is an exempt branch (`HEAD`, `main`, `master`), the version is `${tag}-SNAPSHOT`
       - otherwise, the version is `${tag}-${branch}-SNAPSHOT` (replace `/` in branch name to `_`)

     Special versions:
      "0.0.0-NOGIT" - Git repository not found.
      "0.0.0-NODESC" - Git.describe call failed.
      "0.0.0-FAILURE" - Failed to determine version from git.
     */
    private String calculateVersion(final Project project) {
        try (Repository repo = getRepository(project, throwOnError)) {
            if (repo == null) {
                return NO_GIT_VERSION;
            }

            Git projectGit = Git.wrap(repo);

            try {
                final String desc = projectGit.describe().setLong(true).setTags(true).call();
                if (desc == null)
                    return NO_GIT_DESCRIBE_VERSION;

                final String[] descParts = desc.split("-");
                final int commitAmount = Integer.parseInt(descParts[1]);

                return createVersionString(descParts[0], commitAmount, projectGit.getRepository().getBranch(), exemptBranches::contains);
            } catch (IOException | GitAPIException e) {
                if (throwOnError) {
                    throw new IllegalArgumentException("Failed to determine version string from Git", e);
                } else {
                    project.getLogger().log(LogLevel.ERROR, "Failure to determine version string from Git", e);
                }
                return GENERAL_FAILURE_VERSION;
            }
        }
    }

    /**
     * Creates a version string based on the given arguments.
     *
     * <p>First, if the tag starts with the character {@code "v"}, then that character is stripped from the tag.</p>
     *
     * <p>If the commit amount is zero (indicating that the current commit is directly tagged), then the tag is returned.
     * Otherwise, the branch information will be used to create the version.</p>
     *
     * <p>Before the next rule, if the branch begins with the prefix {@code "pulls/"}, then the number directly after
     * the prefix is used to construct a new branch name of {@code "pr###"} (where {@code ###} is the number).</p>
     *
     * <p>The exempt branch predicate is used to determine if the branch is exempt; if it is exempt, then the version format
     * used will be {@link #EXEMPT_BRANCH_VERSION}. Otherwise, the version format will be {@link #BRANCH_VERSION}.</p>
     *
     * @param tag The version tag, may start with {@code "v"}
     * @param commitAmount The amount of commits since the tag
     * @param branch The current branch, may be {@code null}
     * @param isExempt Predicate for testing if a branch is exempt
     * @return The resulting version string according to the rules above
     */
    public static String createVersionString(String tag, int commitAmount, @Nullable String branch, Predicate<String> isExempt) {
        tag = tag.startsWith("v") ? tag.substring(1) : tag;

        if (commitAmount == 0) { // If directly tagged, use that version
            return tag;
        }

        if (branch != null && branch.startsWith("pulls/")) { // convert the numbered Pull Request branch
            branch = "pr" + branch.split("/", 1)[1]; // pulls/### -> pr###
        }

        if (branch == null || isExempt.test(branch)) { // No branch or exempt branch
            return String.format(EXEMPT_BRANCH_VERSION, tag, commitAmount);
        }

        return String.format(BRANCH_VERSION, tag, commitAmount, branch.replace("/", "_"));
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
