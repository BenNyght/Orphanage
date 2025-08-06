import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.nyght.orphanage"
version = "1.0.0"

repositories {
    mavenCentral()
}

// Configure IntelliJ Platform - using older version for broader compatibility
intellij {
    version.set("2024.1.6") // Stable version compatible with plugin 1.x
    type.set("IC") // IntelliJ Community
    
    // Plugin Dependencies
    plugins.set(listOf())
    
    // Disable instrumentation to avoid path issues
    instrumentCode.set(false)
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("252.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    test {
        useJUnitPlatform()
    }
    
    // Skip problematic tasks
    named("instrumentCode") {
        enabled = false
    }
    
    named("instrumentTestCode") {
        enabled = false
    }
}