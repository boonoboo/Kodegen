plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
}

group = "dk.cachet.rad"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))

    // Include carp.protocols.core library
    implementation("dk.cachet.carp.protocols:carp.protocols.core-jvm:1.0.0-alpha.15")

    // Include rad library
    implementation("dk.cachet.rad:rad:0.0.1")

    // Process annotations using rad
    kapt("dk.cachet.rad:rad:0.0.1")
}