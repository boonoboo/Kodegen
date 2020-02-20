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

    api("io.ktor:ktor-server-jetty:1.3.0")
    api("io.ktor:ktor-client-core:1.3.0")
    api("io.ktor:ktor-client-apache:1.3.0")

    kapt("com.google.auto.service:auto-service:1.0-rc2")
    implementation("com.squareup:kotlinpoet:1.5.0")
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