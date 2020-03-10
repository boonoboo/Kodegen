plugins {
    kotlin("jvm") version "1.3.61" apply false
    kotlin("kapt") version "1.3.61" apply false
    kotlin("plugin.serialization") version "1.3.61" apply false
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.kapt")
    }

    group = "dk.cachet.rad"
    version = "0.0.1"

    repositories {
        jcenter()
        mavenCentral()
    }
}