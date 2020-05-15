plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("io.ktor:ktor-server-core:1.3.2")
    implementation("io.ktor:ktor-server-jetty:1.3.2")
    implementation("io.ktor:ktor-auth:1.3.2")
    implementation("io.ktor:ktor-serialization:1.3.2")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    implementation(project(":rad-study-core"))
}