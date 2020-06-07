plugins {
    kotlin("jvm") version "1.3.72" apply false
    kotlin("kapt") version "1.3.72" apply false
    kotlin("plugin.serialization") version "1.3.72" apply false
}

subprojects {
    repositories {
        jcenter()
        mavenCentral()
    }

    group = "dk.cachet"
    version = "1.0.2"
}