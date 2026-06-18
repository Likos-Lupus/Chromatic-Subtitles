plugins {
    alias(libs.plugins.fabric.loom)
    `maven-publish`
}

val modVersion = libs.versions.modVersion.get()
val targetJava = libs.versions.targetJava.get().toInt()
val minecraftVersion = libs.versions.minecraft.get()

group = "top.likoslupus"
version = "$modVersion+$minecraftVersion"

base {
    archivesName.set("chromaticsubtitles")
}

loom {
    accessWidenerPath.set(file("src/main/resources/chromaticsubtitles.accesswidener"))
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabric.loader)

    implementation(libs.night.config.core)
    implementation(libs.night.config.toml)

    include(libs.night.config.core)
    include(libs.night.config.toml)

    api(libs.jspecify)
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "version" to project.version
            )
        )
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(targetJava))
    }

    val parsed = JavaVersion.toVersion(targetJava)
    sourceCompatibility = parsed
    targetCompatibility = parsed

    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(targetJava)
    options.encoding = "UTF-8"
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName.get()}" }
    }
}
