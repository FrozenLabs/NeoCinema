import net.fabricmc.loom.task.RemapJarTask
import org.apache.tools.ant.taskdefs.condition.Os
import de.undercouch.gradle.tasks.download.Download

plugins {
	java
	id("de.undercouch.download") version "5.4.0"
	id("fabric-loom") version "1.7-SNAPSHOT"
	id("maven-publish")
}

base.archivesName.set(project.property("archives_base_name") as String)
version = project.property("mod_version") as String
group = project.property("maven_group") as String

dependencies {
	"minecraft"("com.mojang:minecraft:${project.property("minecraft_version")}")
	"mappings"("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
	"modImplementation"("net.fabricmc:fabric-loader:${project.property("loader_version")}")
	"modImplementation"("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
	implementation("org.apache.logging.log4j:log4j-api:2.20.0")
	implementation("org.apache.logging.log4j:log4j-core:2.20.0")
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

sourceSets {
	val jcef by creating {
		java.srcDir("java-cef/java")
		java.exclude("**/tests/**")
	}

	named("main") {
		compileClasspath += jcef.output
		runtimeClasspath += jcef.output
	}
}

idea {
	module {
		excludeDirs.add(file("java-cef/java/tests"))
		inheritOutputDirs = true
	}
}

val platforms = listOf("windows_amd64", "linux_amd64", "linux_arm64")
val cefBranch = "main"

tasks.register("downloadJcef") {
	doLast {
		platforms.forEach { platform ->
			try {
				val manifestUrl = uri("https://ewr1.vultrobjects.com/cinemamod-jcef/$cefBranch/$platform/manifest.txt").toURL()
				manifestUrl.readText().lineSequence().forEach { line: String ->
					val (fileHash, relFilePath) = line.trim().split("  ")
					val cefResourceUrl = "https://ewr1.vultrobjects.com/cinemamod-jcef/$cefBranch/$platform/$relFilePath"
					val outputFile = file("${layout.buildDirectory}/cef/$platform/$relFilePath")

					val downloadTask = tasks.create<Download>("download_${platform}_${relFilePath.hashCode()}") {
						src(cefResourceUrl)
						dest(outputFile)
						overwrite(false)
					}

					downloadTask.download()

					if (Os.isFamily(Os.FAMILY_UNIX)) {
						if (relFilePath.contains("chrome-sandbox") || relFilePath.contains("jcef_helper")) {
							exec {
								commandLine("chmod", "700", outputFile.absolutePath)
							}
						}
					}
				}
			} catch (e: Exception) {
				println("Skipping CEF libraries for $cefBranch/$platform")
				println(e.message)
			}
		}
	}
}

fun createPlatformJarTask(platform: String) {
	tasks.register<RemapJarTask>("jar_$platform") {
		inputFile.set(tasks.jar.get().archiveFile)
		dependsOn(tasks.named("jar"), tasks.named("downloadJcef"))

		onlyIf {
			file("${layout.buildDirectory}/cef/$platform").exists()
		}

		into("cef") {
			from("${layout.buildDirectory}/cef/$platform")
		}

		from(sourceSets["main"].output)
		from(sourceSets["jcef"].output)

		archiveAppendix.set(platform)
	}
}

platforms.forEach { platform ->
	createPlatformJarTask(platform)
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
