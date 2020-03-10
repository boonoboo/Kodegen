plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
    application
}

kapt {
    correctErrorTypes = true
    includeCompileClasspath = false
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":rad"))

    //
    implementation("io.ktor:ktor-server-jetty:1.3.0")
    implementation("io.ktor:ktor-server-core:1.3.0")

    // Run kapt using the RAD project
    kapt(project(":rad"))
}

sourceSets["main"].java.srcDir("src")
sourceSets["main"].resources.srcDir("resources")

// Configure the application plugin
application {
    mainClassName = "dk.cachet.rad.example.server.MainKt"
}