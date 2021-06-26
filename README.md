### WrittenBooks

This is the Gradle plugin used by ParchmentMC projects for managing CI-related menial tasks.

#### Usage

Add the following section to your `settings.gradle`:
```groovy
pluginManagement {
    repositories {
        maven {
            name 'ParchmentMC'
            url 'https://maven.parchmentmc.org/'
        }
        gradlePluginPortal()
    }
}
```

Then apply the plugin as normal to your `build.gradle` (substitute `${writtenbooks_version}` with the actual plugin version):
```groovy
plugins {
    id 'org.parchmentmc.writtenbooks' version '${writtenbooks_version}'
}
```

> You **must** supply a version number, otherwise Gradle will not resolve the plugin correctly.

#### License

Copyright (c) 2021 ParchmentMC. This project is licensed under the MIT License (see `LICENSE.txt`).