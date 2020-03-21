plugins {
    kotlin("jvm")
    application
}

dependencies {
    // Include Kotlin
    implementation(kotlin("stdlib"))

    // Include Koin
    implementation("org.koin:koin-core:2.1.3")
    implementation("org.koin:koin-ktor:2.1.3")

    // Include rad-example
    implementation(project(":rad-example"))
}

sourceSets["main"].java.srcDir("src")
sourceSets["main"].resources.srcDir("resources")

// Configure the application plugin
application {
    mainClassName = "dk.cachet.rad.example.server.MainKt"
}