plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "dk.cachet.rad"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

dependencies {
    // Include Kotlin and junit
    implementation(kotlin("stdlib"))
    testImplementation("junit", "junit", "4.12")

    // Include Ktor features
    implementation("io.ktor:ktor-server-core:1.3.2")
    implementation("io.ktor:ktor-server-jetty:1.3.2")
    implementation("io.ktor:ktor-auth:1.3.2")
    implementation("io.ktor:ktor-serialization:1.3.2")





    implementation("ch.qos.logback:logback-classic:1.2.3")

    implementation(project(":rad-study-core"))
}