package org.parchmentmc.writtenbooks.publishing;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.plugins.PublishingPlugin;

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
        Provider<String> usernameEnv = project.getProviders().environmentVariable("LDTTeamJfrogUsername").forUseAtConfigurationTime();
        Provider<String> passwordEnv = project.getProviders().environmentVariable("LDTTeamJfrogPassword").forUseAtConfigurationTime();

        project.getPlugins().withType(PublishingPlugin.class, publishPlugin -> {
            if (usernameEnv.isPresent() && passwordEnv.isPresent()) {
                project.getExtensions().getByType(PublishingExtension.class).getRepositories().maven(maven -> {
                    maven.setName("ParchmentMC");
                    maven.setUrl("https://ldtteam.jfrog.io/artifactory/parchmentmc-internal/");
                    maven.credentials(cred -> {
                        cred.setUsername(usernameEnv.get());
                        cred.setPassword(passwordEnv.get());
                    });
                });
            }
        });
    }
}
