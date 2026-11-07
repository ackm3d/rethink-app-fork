// guardian: code shared by every Guardian app (Android now; iOS, macOS and Windows later).
// Nothing here may depend on Android or on the RethinkDNS engine.
plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvmToolchain(21)
    // consumed by the Android app through the JVM target
    jvm()
    // iOS targets compile only on macOS with Xcode; Gradle skips them elsewhere
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.jetbrainsKotlinxKotlinxCoroutinesCore)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
