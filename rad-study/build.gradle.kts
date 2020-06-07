plugins {
    kotlin("jvm") version "1.3.72" apply false
    kotlin("kapt") version "1.3.72" apply false
    kotlin("plugin.serialization") version "1.3.72" apply false
}

subprojects {
    group = "dk.cachet"
    version = "1.0.1"

    repositories {
        // Include the local repository containing rad-1.0.1
        maven {
            url = uri(rootProject.projectDir.path + "/localRepository")
        }
        mavenCentral()
        jcenter()
    }
}

group = "dk.cachet"
version = "1.0.1"