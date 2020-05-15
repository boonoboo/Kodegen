plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
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