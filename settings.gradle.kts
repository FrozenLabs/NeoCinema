pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        maven(url = "https://maven.minecraftforge.net/")
        mavenCentral()
    }
}

rootProject.name = "NeoCinema"

include("fabric", "bukkit")
