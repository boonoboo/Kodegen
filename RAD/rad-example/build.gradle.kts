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
    implementation(project(":rad"))

    // Run kapt on any annotation processors discovered by AutoService
    kapt(project(":rad"))
}

sourceSets["main"].java.srcDir("src")

/*
kotlin {
    jvm()
    js {
        browser {
        }
        nodejs {
        }
    }

    mingwX64("mingw")
    sourceSets {
        commonMain {
            dependencies {
                implementation kotlin('stdlib-common')
            }
        }
        commonTest {
            dependencies {
                implementation kotlin('test-common')
                implementation kotlin('test-annotations-common')
            }
        }
        jvmMain {
            dependencies {
                implementation kotlin('stdlib-jdk8')
            }
        }
        jvmTest {
            dependencies {
                implementation kotlin('test')
                implementation kotlin('test-junit')
            }
        }
        jsMain {
            dependencies {
                implementation kotlin('stdlib-js')
            }
        }
        jsTest {
            dependencies {
                implementation kotlin('test-js')
            }
        }
        mingwMain {
        }
        mingwTest {
        }
    }
}
 */