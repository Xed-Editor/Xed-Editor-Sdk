plugins {
    kotlin("jvm") version "2.0.10"
    id("java-gradle-plugin")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.rk"
version = "1.0"

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    isZip64 = true
    destinationDirectory.set(file("./output"))
}


gradlePlugin {
    plugins {
        create("XedSdk") {
            id = "com.rk.XedSdk"
            implementationClass = "org.gradle.xed.Main"
        }
    }
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("com.android.tools:r8:8.5.35")
}

kotlin {
    jvmToolchain(17)
}