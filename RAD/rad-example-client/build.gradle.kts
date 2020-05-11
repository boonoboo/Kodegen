plugins {
    kotlin("jvm")
    application
}

dependencies {
    // Include Kotlin
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.4")

    // Include rad and rad-example
    implementation(project(":rad"))
    implementation(project(":rad-example"))

    implementation("io.ktor:ktor-client-core:1.3.2")
    implementation("io.ktor:ktor-client-apache:1.3.2")
    implementation("io.ktor:ktor-client-auth-jvm:1.3.2")
}

sourceSets["main"].java.srcDir("src")
sourceSets["main"].resources.srcDir("resources")

// Configure the application plugin
application {
    mainClassName = "dk.cachet.rad.example.server.MainKt"
}