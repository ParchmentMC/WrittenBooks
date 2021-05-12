package org.parchmentmc.writtenbooks.repositories;

import org.gradle.api.Project;

public class RepositoryManager
{
    private static final RepositoryManager INSTANCE = new RepositoryManager();

    public static RepositoryManager getInstance()
    {
        return INSTANCE;
    }

    private RepositoryManager()
    {
    }

    public void apply(final Project project) {
        project.getRepositories().maven(repo -> {
            repo.setName("ParchmentMC");
            repo.setUrl("https://ldtteam.jfrog.io/artifactory/parchmentmc/");
        });
    }
}
