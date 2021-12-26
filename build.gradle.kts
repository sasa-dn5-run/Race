import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import kotlin.collections.listOf

plugins {
    kotlin("jvm") version("1.6.0")
    id("com.github.johnrengelman.shadow") version "7.1.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.0"
}

group = "run.dn5"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven ("https://maven.enginehub.org/repo/")
    maven ("https://repo.minebench.de/")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.0")
    implementation("de.themoep:inventorygui:1.5-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.8")
}

tasks {
    shadowJar{
        archiveFileName.set("${rootProject.name}-${rootProject.version}.jar")
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    register<Copy>("preDebug"){
        dependsOn("clean", "shadowJar")
        from("$buildDir/libs/${rootProject.name}-${rootProject.version}.jar")
        into("$projectDir/.debug/plugins")
    }
}

bukkit {
    main = "$group.${rootProject.name}.Main"
    apiVersion = "1.18"
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    authors = listOf("ddPn08")
    defaultPermission = BukkitPluginDescription.Permission.Default.OP
    depend = listOf("WorldEdit")

    commands {
        register("race"){
            permission = "race.command.race"
        }
    }
}