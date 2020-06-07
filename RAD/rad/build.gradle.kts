plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")

    // For publishing to local maven repository
    `maven-publish`
    java
}

kapt {
    correctErrorTypes = true
    includeCompileClasspath = false
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    // Include Ktor
    // Transitive dependencies to allow projects using rad to access Ktor features
    api("io.ktor:ktor-client-core:1.3.2")
    api("io.ktor:ktor-client-apache:1.3.2")
    api("io.ktor:ktor-serialization:1.3.2")
    api("io.ktor:ktor-auth:1.3.2")

    // Include serialization runtime
    // Transitive dependency to allow projects using rad to use serialization
    api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")

    // Include AutoService
    implementation("com.google.auto.service:auto-service:1.0-rc6")

    // Include Kotlin Poet
    implementation("com.squareup:kotlinpoet:1.5.0")
    implementation("com.squareup:kotlinpoet-metadata:1.5.0")
    implementation("com.squareup:kotlinpoet-metadata-specs:1.5.0")
    implementation("com.squareup:kotlinpoet-classinspector-elements:1.5.0")

    // Run Kapt
    kapt("com.google.auto.service:auto-service:1.0-rc6")
}

sourceSets["main"].java.srcDir("src")

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

// For publishing to local maven repository
publishing {
    publications {
        create<MavenPublication>("rad") {
            groupId = "dk.cachet"
            artifactId = "rad"
            version = "1.0.1"

            from(components["java"])
        }
    }
}