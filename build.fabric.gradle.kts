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

val clothConfigVersion = when (mcVersion) {
    "26.1.2" -> libs.versions.cloth.config.v2612.get()
    "26.3" -> libs.versions.cloth.config.v263.get()
    else -> error("No Cloth Config version configured for Minecraft $mcVersion")
}
val modMenuVersion = when (mcVersion) {
    "26.1.2" -> libs.versions.modmenu.v2612.get()
    "26.3" -> libs.versions.modmenu.v263.get()
    else -> error("No Mod Menu version configured for Minecraft $mcVersion")
}

group = providers.gradleProperty("maven_group").get()
version = "$modVersion+$mcVersion"
base.archivesName.set("$modId-fabric")

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/") {
        name = "Fabric"
    }
    maven("https://maven.shedaniel.me/") {
        name = "Shedaniel"
    }
    maven("https://maven.terraformersmc.com/releases/") {
        name = "TerraformersMC"
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    loomx.applyMojangMappings()

    modImplementation(libs.fabric.loader)

    implementation(libs.night.config.core)
    implementation(libs.night.config.toml)

    include(libs.night.config.core)
    include(libs.night.config.toml)

    // Optional config GUI. These dependencies are compile-time only; Cloth and Mod Menu are
    // soft dependencies declared in fabric.mod.json and must not be bundled into the mod jar.
    modCompileOnly("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion") {
        isTransitive = false
    }
    compileOnly(libs.basic.math)
    modCompileOnly("com.terraformersmc:modmenu:$modMenuVersion")

    // Development-only runtime dependencies. Use -Pwith_cloth=false / -Pwith_modmenu=false to test
    // the optional dependency paths.
    if (providers.gradleProperty("with_cloth").orNull != "false") {
        modLocalRuntime("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion")
    }
    if (providers.gradleProperty("with_modmenu").orNull != "false") {
        modLocalRuntime("com.terraformersmc:modmenu:$modMenuVersion")
    }

    compileOnly(libs.jspecify)
}

loom {
    fabricModJsonPath = rootProject.file("src/fabric/resources/fabric.mod.json")
    accessWidenerPath = rootProject.file("src/main/resources/chromaticsubtitles.accesswidener")

    runConfigs.configureEach {
        runDir(rootProject.file("run").path)
    }
}

sourceSets["main"].apply {
    java.srcDir(rootProject.file("src/fabric/java"))
    resources.srcDir(rootProject.file("src/fabric/resources"))
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
        "loader_version" to libs.versions.fabric.loader.get(),
    )

    inputs.properties(props)
    filesMatching("fabric.mod.json") {
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
    description = "Builds the Fabric jar and copies it into the root build/libs directory."
    dependsOn(tasks.named("build"))
    from(loomx.modJar, loomx.modSourcesJar)
    into(rootProject.layout.buildDirectory.dir("libs/${sc.current.project}"))
}
