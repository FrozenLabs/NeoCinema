import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

repositories {
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        content {
            includeGroup("org.bukkit")
            includeGroup("org.spigotmc")
        }
    }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    maven { url = uri("https://maven.elmakers.com/repository") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/central") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
    mavenCentral()
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot:1.21.4-R0.1-SNAPSHOT")
    compileOnly("io.netty:netty-all:4.1.115.Final")
    compileOnly("org.xerial:sqlite-jdbc:3.47.0.0")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.13")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
    implementation("org.bytedeco:ffmpeg:6.1.1-1.5.10")
    implementation("org.bytedeco:ffmpeg:6.1.1-1.5.10:windows-x86_64")
    implementation("org.bytedeco:ffmpeg:6.1.1-1.5.10:linux-x86_64")
    implementation("org.bytedeco:ffmpeg:6.1.1-1.5.10:linux-arm64")
    implementation("org.bytedeco:ffmpeg:6.1.1-1.5.10:macosx-x86_64")
    implementation("org.bytedeco:ffmpeg:6.1.1-1.5.10:macosx-arm64")
}

tasks {
    jar {
        enabled = false
    }

    runServer {
        minecraftVersion("1.21.4")
    }

    assemble {
        dependsOn(named("shadowJar"))
    }

    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("LunarCinemas-${project.parent?.version}")
    }
}
