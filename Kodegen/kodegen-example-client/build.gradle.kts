plugins {
    kotlin("jvm")
    application
}

dependencies {
    // Include Kotlin
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.4")

    // Include kodegen and kodegen-example
    implementation(project(":kodegen"))
    implementation(project(":kodegen-example"))

    implementation("io.ktor:ktor-client-core:1.3.2")
    implementation("io.ktor:ktor-client-apache:1.3.2")
    implementation("io.ktor:ktor-client-auth-jvm:1.3.2")
}

sourceSets["main"].java.srcDir("src")
sourceSets["main"].resources.srcDir("resources")

// Configure the application plugin
application {
    mainClassName = "dk.cachet.kodegen.example.server.MainKt"
}