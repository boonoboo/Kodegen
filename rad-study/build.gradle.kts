plugins {
    kotlin("jvm") version "1.3.72" apply false
    kotlin("kapt") version "1.3.72" apply false
    kotlin("plugin.serialization") version "1.3.72" apply false
}

subprojects {
    group = "dk.cachet.rad"
    version = "1.0.0"

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }
}

group = "dk.cachet.rad"
version = "1.0.0"