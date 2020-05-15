plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("dk.cachet.carp.protocols:carp.protocols.core-jvm:1.0.0-alpha.15")

    implementation("dk.cachet.rad:rad:1.0.0")
    kapt("dk.cachet.rad:rad:1.0.0")
}