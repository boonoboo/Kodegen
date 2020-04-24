plugins {
    java
    kotlin("jvm") version "1.3.72"
}

group = "dk.cachet.rad"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("dk.cachet.carp.protocols:carp.protocols.core-jvm:1.0.0-alpha.15")

    implementation("dk.cachet.rad:rad:0.0.1")

    implementation(kotlin("stdlib"))
    testImplementation("junit", "junit", "4.12")
}

/*
configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}*/