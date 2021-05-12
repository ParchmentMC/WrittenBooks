package org.parchmentmc.writtenbooks.publishing;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.publish.PublishingExtension;

public class PublishingManager
{
    private static final PublishingManager INSTANCE = new PublishingManager();

    public static PublishingManager getInstance()
    {
        return INSTANCE;
    }

    private PublishingManager()
    {
    }

    public void apply(final Project project) {
        project.getPlugins().apply("maven-publish");

        if (System.getenv().containsKey("LDTTeamJfrogUsername") && System.getenv().containsKey("LDTTeamJfrogPassword")) {
            project.getExtensions().configure("publishing", (Action<PublishingExtension>) pubEx -> pubEx.repositories(repos -> repos.maven(maven -> {
                maven.setName("ParchmentMC");
                maven.setUrl("https://ldtteam.jfrog.io/artifactory/parchmentmc-internal/");
                maven.getCredentials().setUsername(System.getenv().get("LDTTeamJfrogUsername"));
                maven.getCredentials().setPassword(System.getenv().get("LDTTeamJfrogPassword"));
            })));
        }
    }
}
