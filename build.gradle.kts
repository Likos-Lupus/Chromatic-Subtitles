plugins {
    alias(libs.plugins.fabric.loom)
    `maven-publish`
}

val modVersion = libs.versions.modVersion.get()
val targetJava = libs.versions.targetJava.get().toInt()
val minecraftVersion = libs.versions.minecraft.get()
val loaderVersion = libs.versions.fabric.loader.get()

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
    implementation(libs.fabric.loader)

    implementation(libs.night.config.core)
    implementation(libs.night.config.toml)

    include(libs.night.config.core)
    include(libs.night.config.toml)

    api(libs.jspecify)
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "loaderVersion" to loaderVersion,
        "minecraftVersion" to minecraftVersion
    )

    inputs.properties(props)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(props)
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
