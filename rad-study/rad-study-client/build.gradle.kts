plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "dk.cachet.rad"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.6")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")

    implementation("io.ktor:ktor-client-core:1.3.2")
    implementation("io.ktor:ktor-client-apache:1.3.2")
    implementation("io.ktor:ktor-client-auth-jvm:1.3.2")

    implementation(project(":rad-study-core"))
}