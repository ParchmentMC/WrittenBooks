package org.parchmentmc.writtenbooks.publishing;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.plugins.PublishingPlugin;

public class PublishingManager {
    private final Provider<String> repository;
    private final Provider<String> username;
    private final Provider<String> password;

    public PublishingManager(Provider<String> repository, Provider<String> username, Provider<String> password) {
        this.repository = repository.forUseAtConfigurationTime();
        this.username = username.forUseAtConfigurationTime();
        this.password = password.forUseAtConfigurationTime();
    }

    public void apply(final Project project) {
        project.afterEvaluate(p -> {
            p.getPlugins().withType(PublishingPlugin.class, publishPlugin -> {
                if (username.isPresent() && password.isPresent()) {
                    p.getExtensions().getByType(PublishingExtension.class).getRepositories().maven(maven -> {
                        maven.setName("ParchmentMC");
                        maven.setUrl(repository.get());
                        p.getLogger().debug("Set publishing Maven repository to '{}'", maven.getUrl());
                        maven.credentials(cred -> {
                            cred.setUsername(username.get());
                            cred.setPassword(password.get());
                        });
                    });
                }
            });
        });
    }
}
