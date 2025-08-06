import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.nyght.orphanage"
version = "0.0.5"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin with minimal settings
intellij {
    version.set("2024.1.6") // More stable version
    type.set("IC")
    
    plugins.set(listOf())
    instrumentCode.set(false)
    sandboxDir.set("${project.buildDir}/idea-sandbox")
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
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

    test {
        useJUnitPlatform()
    }
    
    // Simplify build process
    buildPlugin {
        dependsOn("jar")
        archiveFileName.set("orphanage-rider-plugin-${project.version}.zip")
    }
    
    // Skip problematic tasks
    named("publishPlugin") {
        enabled = false
    }
    
    named("signPlugin") {
        enabled = false
    }
}