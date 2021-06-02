package org.parchmentmc.writtenbooks.publishing;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.plugins.PublishingPlugin;

public class PublishingManager {
    private static final String GH_URL = "https://github.com/%s";
    private static final String GH_URL_ISSUES = "https://github.com/%s/issues";
    private static final String GH_CONNECTION = "scm:git:git://github.com/%s.git";
    private static final String GH_DEV_CONNECTION = "scm:git:git@github.com/%s.git";

    private final Provider<String> repository;
    private final Provider<String> username;
    private final Provider<String> password;
    private final Provider<String> ghRepo;

    public PublishingManager(Provider<String> repository, Provider<String> username, Provider<String> password, Provider<String> ghRepo) {
        this.repository = repository.forUseAtConfigurationTime();
        this.username = username.forUseAtConfigurationTime();
        this.password = password.forUseAtConfigurationTime();
        this.ghRepo = ghRepo.forUseAtConfigurationTime();
    }

    public void apply(final Project project) {
        project.getPlugins().withType(PublishingPlugin.class, publishPlugin -> {
            final PublishingExtension ext = project.getExtensions().getByType(PublishingExtension.class);

            ext.getPublications().withType(MavenPublication.class).all(pub -> pub.pom(pom -> {
                pom.getUrl().convention(ghRepo.map(s -> String.format(GH_URL, s)));
                pom.scm(scm -> {
                    scm.getUrl().convention(ghRepo.map(s -> String.format(GH_URL, s)));
                    scm.getConnection().convention(ghRepo.map(s -> String.format(GH_CONNECTION, s)));
                    scm.getDeveloperConnection().convention(ghRepo.map(s -> String.format(GH_DEV_CONNECTION, s)));
                });
                pom.issueManagement(issue -> {
                    // Makes sure that we only set the system name if the repo is there
                    issue.getSystem().convention(ghRepo.map(s -> "GitHub Issues"));
                    issue.getUrl().convention(ghRepo.map(s -> String.format(GH_URL_ISSUES, s)));
                });
            }));

            project.afterEvaluate(p -> {
                if (username.isPresent() && password.isPresent()) {
                    ext.getRepositories().maven(maven -> {
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
