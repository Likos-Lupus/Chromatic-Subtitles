/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2021 haykam821
 * Copyright (c) 2026 Likos-Lupus and Chromatic Subtitles contributors
 */

plugins {
    id("dev.kikugie.loom-back-compat")
}

val modId = providers.gradleProperty("archives_base_name").get()
val modVersion = providers.gradleProperty("mod_version").get()
val mcVersion = sc.current.version
val javaVersion = libs.versions.java.get()

val neoForgeVersion = when (mcVersion) {
    "26.1.2" -> libs.versions.neoforge.v2612.get()
    "26.3" -> libs.versions.neoforge.v263.get()
    else -> error("No NeoForge version configured for Minecraft $mcVersion")
}
val clothConfigVersion = when (mcVersion) {
    "26.1.2" -> libs.versions.cloth.config.v2612.get()
    "26.3" -> libs.versions.cloth.config.v263.get()
    else -> error("No Cloth Config version configured for Minecraft $mcVersion")
}

group = providers.gradleProperty("maven_group").get()
version = "$modVersion+$mcVersion"
base.archivesName.set("$modId-neoforge")

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases/") {
        name = "NeoForged"
    }
    maven("https://maven.shedaniel.me/") {
        name = "Shedaniel"
    }
}

// Architectury Loom registers the `neoForge` configuration lazily, so the loader dependency is
// attached as soon as the configuration appears.
val dependencyHandler = dependencies
configurations.matching { it.name == "neoForge" }.configureEach {
    dependencyHandler.add(
        name,
        "net.neoforged:neoforge:$neoForgeVersion"
    )
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    loomx.applyMojangMappings()

    implementation(libs.night.config.core)
    implementation(libs.night.config.toml)

    include(libs.night.config.core)
    include(libs.night.config.toml)

    // Optional config GUI; soft dependency declared in neoforge.mods.toml.
    modCompileOnly("me.shedaniel.cloth:cloth-config-neoforge:$clothConfigVersion")
    if (providers.gradleProperty("with_cloth").orNull != "false") {
        modLocalRuntime("me.shedaniel.cloth:cloth-config-neoforge:$clothConfigVersion")
    }

    compileOnly(libs.jspecify)
}

loom {
    neoForge {
        // AW -> AT conversion is not available for 26.1+ no-remap builds, so NeoForge gets a
        // hand-written access transformer for the package-private SubtitleOverlay.Subtitle class.
        accessTransformer(rootProject.file("src/neoforge/resources/META-INF/accesstransformer.cfg"))
    }

    runConfigs.configureEach {
        runDir(rootProject.file("run").path)
    }
}

sourceSets["main"].apply {
    java.srcDir(rootProject.file("src/neoforge/java"))
    resources.srcDir(rootProject.file("src/neoforge/resources"))
}

java {
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion.toInt()))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(javaVersion.toInt())
    options.encoding = "UTF-8"
}

tasks.named<ProcessResources>("processResources") {
    val props = mapOf(
        "version" to project.version.toString(),
        "minecraft_version" to mcVersion,
        "neoforge_version" to neoForgeVersion,
    )

    inputs.properties(props)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(props)
    }
    filesMatching("chromaticsubtitles.mixin.json") {
        expand("java" to "JAVA_$javaVersion")
    }
}

tasks.withType<Jar>().configureEach {
    from(rootProject.file("LICENSE")) {
        rename { "${it}_$modId" }
    }
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    description = "Builds the NeoForge jar and copies it into the root build/libs directory."
    dependsOn(tasks.named("build"))
    from(loomx.modJar, loomx.modSourcesJar)
    into(rootProject.layout.buildDirectory.dir("libs/${sc.current.project}"))
}
