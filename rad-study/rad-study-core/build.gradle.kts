plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")

    api("dk.cachet.carp.protocols:carp.protocols.core-jvm:1.0.0-alpha.15")

    implementation("dk.cachet:rad:1.0.1")
    kapt("dk.cachet:rad:1.0.1")
}

kapt {
    correctErrorTypes = true
}