val ktor_version = "1.3.2"

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-jetty:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    implementation(project(":rad-study-core"))
}

application {
    mainClassName = "dk.cachet.rad.study.ServerApplicationKt"
}