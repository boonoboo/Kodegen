import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("kapt")
}

kapt {
    correctErrorTypes = true
    includeCompileClasspath = false
}

// Set Kotlin to compile bytecode to Java 8 instead of default Java 6
/*
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
*/

dependencies {
    implementation(kotlin("stdlib"))

    // Transitive dependencies to allow projects using RAD to access Ktor features
    api("io.ktor:ktor-server-jetty:1.3.0")
    api("io.ktor:ktor-client-core:1.3.0")
    api("io.ktor:ktor-client-apache:1.3.0")
    api("io.ktor:ktor-serialization:1.3.0")

    kapt("com.google.auto.service:auto-service:1.0-rc2")
    implementation("com.squareup:kotlinpoet:1.5.0")
    //implementation("com.squareup:kotlinpoet-metadata:1.5.0")
    //implementation("com.squareup:kotlinpoet-metadata-specs:1.5.0")
    //implementation("me.eugeniomarletti.kotlin.metadata:kotlin-metadata:1.4.0")
    implementation("com.google.auto.service:auto-service:1.0-rc2")
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