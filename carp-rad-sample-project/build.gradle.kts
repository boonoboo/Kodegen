plugins {
    java
    kotlin("jvm") version "1.3.72"
    kotlin("kapt") version "1.3.72"
    kotlin("plugin.serialization") version "1.3.72"
}

group = "dk.cachet.rad"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

dependencies {
    // Include Kotlin and junit
    implementation(kotlin("stdlib"))
    testImplementation("junit", "junit", "4.12")

    // Include carp.protocols.core library
    implementation("dk.cachet.carp.protocols:carp.protocols.core-jvm:1.0.0-alpha.15")

    implementation("io.ktor:ktor-server-core:1.3.2")
    implementation("io.ktor:ktor-server-jetty:1.3.2")
    implementation("io.ktor:ktor-auth:1.3.2")
    implementation("io.ktor:ktor-client-core:1.3.2")
    implementation("io.ktor:ktor-client-apache:1.3.2")
    implementation("io.ktor:ktor-client-auth-jvm:1.3.2")

    // Include rad library
    implementation("dk.cachet.rad:rad:0.0.1")

    // Process annotations using rad
    kapt("dk.cachet.rad:rad:0.0.1")
}