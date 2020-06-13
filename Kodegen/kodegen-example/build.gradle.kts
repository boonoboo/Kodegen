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
    // Include Kotlin
    implementation(kotlin("stdlib"))

    // Include kodegen
    implementation(project(":kodegen"))

    // Include Ktor features
    implementation("io.ktor:ktor-server-jetty:1.3.2")
    implementation("io.ktor:ktor-server-core:1.3.2")
    implementation("io.ktor:ktor-auth:1.3.2")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    // Run kapt using kodegen
    kapt(project(":kodegen"))
}

sourceSets["main"].java.srcDir("src")
sourceSets["main"].resources.srcDir("resources")

// Configure the application plugin
application {
    mainClassName = "dk.cachet.kodegen.example.server.MainKt"
}
