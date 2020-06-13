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
    // Transitive dependencies to allow projects using kodegen to access Ktor features
    api("io.ktor:ktor-client-core:1.3.2")
    api("io.ktor:ktor-client-apache:1.3.2")
    api("io.ktor:ktor-serialization:1.3.2")
    api("io.ktor:ktor-auth:1.3.2")

    // Include serialization runtime
    // Transitive dependency to allow projects using kodegen to use serialization
    api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")

    // Include AutoService and gradle-incap-helper to automatically generate META-INF files
    implementation("com.google.auto.service:auto-service:1.0-rc6")
    implementation("net.ltgt.gradle.incap:incap:0.2")
    compileOnly("net.ltgt.gradle.incap:incap-processor:0.2")
    kapt("net.ltgt.gradle.incap:incap-processor:0.2")


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
        create<MavenPublication>("kodegen") {
            groupId = "dk.cachet"
            artifactId = "kodegen"
            version = "1.0.0"

            from(components["java"])
        }
    }
}