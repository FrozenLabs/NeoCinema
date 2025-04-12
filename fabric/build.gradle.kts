import org.gradle.kotlin.dsl.java

plugins {
	java
	id("de.undercouch.download") version "5.4.0"
	id("fabric-loom") version "1.7-SNAPSHOT"
	id("maven-publish")
}

base.archivesName.set(project.property("archives_base_name") as String)
version = project.property("mod_version") as String
group = project.property("maven_group") as String

repositories {
	maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
	maven {
		url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
	}
}

dependencies {
	"minecraft"("com.mojang:minecraft:${project.property("minecraft_version")}")
	"mappings"("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
	"modImplementation"("net.fabricmc:fabric-loader:${project.property("loader_version")}")
	"modImplementation"("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
	implementation("org.apache.logging.log4j:log4j-api:2.20.0")
	implementation("org.apache.logging.log4j:log4j-core:2.20.0")
	modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.1")
	implementation("uk.co.caprica:vlcj:5.0.0-SNAPSHOT")
	implementation("uk.co.caprica:vlcj-natives:5.0.0-SNAPSHOT")
}

tasks.runClient {
	jvmArgs("-Ddevauth.enabled=true")
}

tasks.processResources {
	inputs.property("version", project.version)

	filesMatching("fabric.mod.json") {
		expand(mapOf("version" to project.version))
	}

	filesMatching("cinemamod.mixins.json") {
		expand(project.properties)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release.set(21)
}

java {
	withSourcesJar()
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
	from("LICENSE") {
		rename { "${it}_${base.archivesName.get()}" }
	}
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}

	repositories {
		// Define your Maven publishing destinations here
	}
}
