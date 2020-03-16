import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("kapt")
}

kapt {
    correctErrorTypes = true
    includeCompileClasspath = false
}

dependencies {
    implementation(kotlin("stdlib"))

    // Include Ktor
    // Transitive dependencies to allow projects using RAD to access Ktor features
    api("io.ktor:ktor-server-jetty:1.3.2")
    api("io.ktor:ktor-client-core:1.3.2")
    api("io.ktor:ktor-client-apache:1.3.2")
    api("io.ktor:ktor-serialization:1.3.2")

    // Include Koin
    api("org.koin:koin-core:2.1.3")
    api("org.koin:koin-ktor:2.1.3")

    // Include AutoService
    implementation("com.google.auto.service:auto-service:1.0-rc6")

    // Include Kotlin Poet
    implementation("com.squareup:kotlinpoet:1.5.0")
    implementation("com.squareup:kotlinpoet-metadata:1.5.0")
    implementation("com.squareup:kotlinpoet-metadata-specs:1.5.0")
    implementation("com.squareup:kotlinpoet-classinspector-elements:1.5.0")
    implementation("com.squareup:kotlinpoet-classinspector-reflective:1.5.0")

    // Run Kapt
    kapt("com.google.auto.service:auto-service:1.0-rc6")
}

sourceSets["main"].java.srcDir("src")

// Create new Gradle task for processing Rad annotations
tasks.register("rad") {
    doLast {
        println("Processing RAD annotations")
    }
}

repositories {
    mavenCentral()
    jcenter()
}