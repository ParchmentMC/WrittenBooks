### WrittenBooks
This repository contains a gradle plugin that is used to manage several CI related tasks within Parchments projects efficiently.

#### Usage
Add the following section to your `settings.gradle`:
```groovy
pluginManagement {
    repositories {
        maven {
            name 'ParchmentMC'
            url 'https://ldtteam.jfrog.io/artifactory/parchmentmc/'
        }
        gradlePluginPortal()
    }
}
```
Then add the plugin as normal to your `build.gradle`:
```groovy
plugins {
    id 'org.parchmentmc.writtenbooks' version '0.0.0'
}
```
Substituting the version number (here `0.0.0`) with the requested version number.
_You have to supply a version number, else Gradle will not resolve plugins which are not published in its own Repository...._

#### Current objectives
- Setting the version number to a scheme that matches the used scheme by Parchment

#### Current gradle tasks
None

#### Contributing
1. Fork the repository.
2. Make your changes to your fork.
3. Create a PR.
4. Await your Review
5. Enjoy.